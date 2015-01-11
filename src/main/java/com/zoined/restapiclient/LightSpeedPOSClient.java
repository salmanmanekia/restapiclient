package com.zoined.restapiclient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.zoined.lightspeedpos.modules.AccountItem.Item;
import com.zoined.lightspeedpos.modules.AccountSale.Sale;
import com.zoined.lightspeedpos.modules.AccountSale.SaleLine;

/*
 * LightSpeedPOSClient class implements an application 
 * that creates HTTP requests for Item and Sale modules from lightspeedpos API.
 * The response from the API is parsed to a String and then 
 * the JSON String is map to POJO by using Jackson. 
 * The CSV is created out of those POJO by using SuperCSV 
 * 'Writing CSV File' and 'Writing CSV File with Dozer'
 * for Item and Sale modules respectively.
 * 
 * @author 	Muhammad Salman
 * @version 0.0.1 
 */

public class LightSpeedPOSClient {

	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final String BASE_URL = "https://api.merchantos.com/API/Account/";
	private static final String ACCOUNT_ID = "101584";
	private static final String ACCESS_TOKEN = "907737989deadf83929caa127bac6abbcd51671f"; 
	private static final long START_OF_2015 = 142_007_040_000_0l;
	private static final String TIME_STAMP = "TIME_STAMP";
	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	private  static final Preferences PREFS = Preferences.userRoot().node(LightSpeedPOSClient.class.getName());
		
	// property for the item fields in the csv
	private static final CellProcessor[] ITEM_PROCESSOR = new CellProcessor[] {
        new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), 
        new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), 
        new Optional(),	new Optional(), new Optional() 
	};
	
	// mapping to AccountItem's properties
	private static final String[] ITEM_FIELD_MAPPING = new String[] { 
		"systemSku",
		"description", 
        "note.note",
        "category.name", 
		"avgCost", 
		"lastPrice",
		"suppliers",
		"productBrands",
		"vatRate",
		"unitOfMeasure",
		"size",
		"customFieldValues.customFieldValue.value",
		"itemType"};
	
	// header for the items csv file
	private static final String[] ITEM_HEADER = new String[] {
		"products_nk", 
		"products_name", 
		"products_description", 
		"categories_nk", 
		"average_price",
		"last_price",
		"suppliers_nk", 
		"products_brand", 
		"vat_rate", 
		"unit_of_measure", 
		"size",
		"color",
		"type"
	};

	// header for the sales csv file
	private static final String[] SALE_HEADER = new String[] {
		"header_nk",
		"line_item_nk",
		"customers_nk",
		"products_nk",
		"suppliers_nk",
		"unit_sales_price",
		"products_quantity",
		 "sales_datetime",
		"organisation_nk",
		"salespersons_nk",
		"campaigns_nk",
		"sales_value",
		"sales_value_wo_vat",
		"sales_currency",
		"purchase_price",
		"purchase_price_wo_vat",
		"purchase_currency",
		"payment_method",
		"piece_good"
	};
	
	// property for the sale fields in the csv
	private static final CellProcessor[] SALE_PROCESSOR = new CellProcessor[] {
        new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), 
        new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), 
        new Optional(), new Optional(), new Optional(), new Optional(), new Optional(),
        new Optional(),	new Optional(), new Optional() , new Optional()
	};


	public static enum Module{
		// (Name, Extension, ClassName, CSV, JSONFieldName)
		ITEM("Item", ".json", "Item", "products.csv", "Item"),
		SALE("Sale", ".json", "Sale", "sales.csv", "Sale");
		
		private final String name;
		private final String uriExtension;
		private final String className;
		private final String csvFileName;
		private final String jsonFieldName;
	
		private Module(final String name,
				final String uriExtension,
				final String className, 
				final String csvFileName, 
				final String jsonFieldName) {
			
			this.name =name;
			this.uriExtension = uriExtension;
			this.className = className;
			this.csvFileName = csvFileName;
			this.jsonFieldName = jsonFieldName;
		}
	}
	
	// Static class defining the behavior of the application when unsuccessful response occurs from the server
	public static class RequestInitializer implements HttpRequestInitializer, HttpUnsuccessfulResponseHandler {

		@Override
		public void initialize(HttpRequest request) throws IOException {
			request.setUnsuccessfulResponseHandler(this);
		}

		@Override
		public boolean handleResponse(HttpRequest request, HttpResponse response,
				boolean retrySupported) throws IOException {
			int statusCode = response.getStatusCode();
			if (statusCode == 503) {
				try {
					Thread.sleep(60000);
					return true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return false;
		}	
	} 
	
	// Creates a HTTP request. Sets the URL and the Header and execute the request asynchronously.
	private static Future<HttpResponse> executeRequest(final Module module, final String prevDate, Executor executor) throws IOException{
		
		HttpRequestFactory requestFactory = 
				HTTP_TRANSPORT.createRequestFactory(new RequestInitializer());

		GenericUrl url = new GenericUrl(BASE_URL + ACCOUNT_ID + "/" + module.name + module.uriExtension);
		url =  url.set("load_relations", "all").set("timeStamp", ">=," + prevDate);
	    HttpRequest request = requestFactory.buildGetRequest(url);
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAuthorization("OAuth " + ACCESS_TOKEN);
		headers.setAccept("application/vnd.merchantos­v2+json");				
		request.setHeaders(headers);

	    return request.executeAsync(executor);
		
	}
	
	
	private static Future<HttpResponse> executeRequestWithoutPrevDate(final Module module, Executor executor) throws IOException{
		
		HttpRequestFactory requestFactory = 
				HTTP_TRANSPORT.createRequestFactory(new RequestInitializer());

		GenericUrl url = new GenericUrl(BASE_URL + ACCOUNT_ID + "/" + module.name + module.uriExtension);
		url =  url.set("load_relations", "all");
	    HttpRequest request = requestFactory.buildGetRequest(url);
	    
		HttpHeaders headers = new HttpHeaders();
	    headers.setAuthorization("OAuth " + ACCESS_TOKEN);
		headers.setAccept("application/vnd.merchantos­v2+json");		
		request.setHeaders(headers);
		
	    return request.executeAsync(executor);	
		
	}
	

	// Gets the TimeStamp of previous application execution. 
	public static String getPreviousDate() {
		long prevDateLong = PREFS.getLong(TIME_STAMP, START_OF_2015);
		Date prevDate = new Date(prevDateLong);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN, Locale.US);
		return simpleDateFormat.format(prevDate);
	}
	
	// A generic function to read the JSON and map the JSON to the POJO class
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E, T extends Collection> T readFromJsonAndFillType (
			String json, 
			Module module,
			Class <T> collectionType,
			Class <E> elementType) 
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper objMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		
		TypeFactory tf = objMapper.getTypeFactory();
		JsonNode node = objMapper.readTree(json).get(module.jsonFieldName);
		if (node == null) {
			return (T) new ArrayList<E>();
		} else {
			return objMapper.readValue(node.toString(),
					tf.constructCollectionType(collectionType, elementType));
		}
		
	}
	
	// Writes the module data to the CSV.
	// Writes the module's field information to the CSV file 
	public static <T> void writeToCsv(ArrayList<T> moduleList, Module module, CellProcessor []processor) {
		ICsvDozerBeanWriter itemBeanWriter = null;
		ICsvMapWriter saleMapWriter = null;
		try {
			// Uses the Dozer write to map the Item class field to the item CSV 
			if (module == Module.ITEM) {
				itemBeanWriter = 
						new CsvDozerBeanWriter(new FileWriter("target//" + module.csvFileName),
		                CsvPreference.STANDARD_PREFERENCE);
				itemBeanWriter.configureBeanMapping(Item.class, ITEM_FIELD_MAPPING);
		        itemBeanWriter.writeHeader(ITEM_HEADER);
		        for(T mod : moduleList ) {
		             itemBeanWriter.write(mod, processor);
		        }
		    // Uses the write CSV to write the Sale class field to the sale CSV manually.
		    // The mapping was not possible due to deeply nested JSON structure of the Sale JSON.
			} else if (module == Module.SALE) {
				
				final Map<String, Object> salesMap = new HashMap<String, Object>();
				saleMapWriter = new CsvMapWriter(new FileWriter("target//" + module.csvFileName),
						CsvPreference.STANDARD_PREFERENCE);
	
				saleMapWriter.writeHeader(SALE_HEADER);
				
				for (T mod : moduleList){
					String total = ((Sale) mod).getSaleTotal();
					String salesValueWOVat = Integer.toString(((Sale) mod).getSalesValueWOVat());
					String sPAmount = "";
					String sLItemId = "";
					String sLItemDesc = "";
					String sLUnitPrice = "";
					String sLUnitQuantity = "";
					String sLTimeStamp = "";
					String sPTName = "";
					
					if (((Sale) mod).getSalePayments() != null && ((Sale) mod).getSalePayments().getSalePayment() != null) {	
						sPAmount = ((Sale) mod).getSalePayments().getSalePayment().getAmount();
						if (((Sale) mod).getSalePayments().getSalePayment().getSalePaymentType() != null) {
							sPTName = ((Sale) mod).getSalePayments().getSalePayment().getSalePaymentType().getName();
						}
					}
					
					if (((Sale) mod).getSLs() != null && ((Sale) mod).getSLs().getSaleLines() != null ) {
						ArrayList<SaleLine> saleLine = (ArrayList<SaleLine>) ((Sale) mod).getSLs().getSaleLines();
						for  (SaleLine sl:saleLine){
							sLItemId = sl.getItemId();
							sLUnitPrice = sl.getUnitPrice();
							sLUnitQuantity = sl.getUnitQuantity();
							sLTimeStamp = sl.getTimeStamp();
							
							if (sl.getSaleLineItem() != null){
								sLItemDesc = sl.getSaleLineItem().getDescription();
							}
						}
					}
					salesMap.put(SALE_HEADER[0], "");
					salesMap.put(SALE_HEADER[1], sLItemId);
					salesMap.put(SALE_HEADER[2], "");
					salesMap.put(SALE_HEADER[3], sLItemDesc);
					salesMap.put(SALE_HEADER[4], "");
					salesMap.put(SALE_HEADER[5], sLUnitPrice);
					salesMap.put(SALE_HEADER[6], sLUnitQuantity);
					salesMap.put(SALE_HEADER[7], sLTimeStamp);
					salesMap.put(SALE_HEADER[8], "");
					salesMap.put(SALE_HEADER[9], "");
					salesMap.put(SALE_HEADER[10], "");
					salesMap.put(SALE_HEADER[11], total);
					salesMap.put(SALE_HEADER[12], salesValueWOVat);
					salesMap.put(SALE_HEADER[13], "");
					salesMap.put(SALE_HEADER[14], sPAmount);
					salesMap.put(SALE_HEADER[15], "");
					salesMap.put(SALE_HEADER[16], "");
					salesMap.put(SALE_HEADER[17], sPTName);
					salesMap.put(SALE_HEADER[18], "");

					saleMapWriter.write(salesMap, SALE_HEADER, processor);
				}
			} 
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			try {
				if( itemBeanWriter != null ) {
	                itemBeanWriter.close();
				}
				if ( saleMapWriter != null ) {
					saleMapWriter.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	} 
	
	public static void main(String []args) throws JsonParseException, JsonMappingException, IOException {
		ExecutorService executor = Executors.newCachedThreadPool();		

		try {
			String prevDateString = getPreviousDate();

			Future<HttpResponse> itemResponse = executeRequest(Module.ITEM, prevDateString, executor);		
			Future<HttpResponse> saleResponse = executeRequest(Module.SALE, prevDateString, executor);
		
			String itemData = itemResponse.get().parseAsString();
			String saleData = saleResponse.get().parseAsString();	
		
			@SuppressWarnings("unchecked")
			ArrayList<Item> itemList = readFromJsonAndFillType(itemData, Module.ITEM, ArrayList.class, Item.class);
			if (itemList.isEmpty()) {
				System.out.println("No new Item data since the last runs");
			} else {
				writeToCsv(itemList, Module.ITEM, ITEM_PROCESSOR);
			}
	
			@SuppressWarnings("unchecked")
			ArrayList<Sale> saleList = readFromJsonAndFillType(saleData, Module.SALE, ArrayList.class, Sale.class);
			if (saleList.isEmpty()) {
				System.out.println("No new Sale data since the last runs");
			} else {
				writeToCsv(saleList, Module.SALE, SALE_PROCESSOR);
			}
			PREFS.putLong(TIME_STAMP, new Date().getTime());
			
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		} finally {
			executor.shutdown();
		}
	}
}
