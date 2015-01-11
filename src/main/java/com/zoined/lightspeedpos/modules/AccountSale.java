package com.zoined.lightspeedpos.modules;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// POJO class for Sale module. Maps the JSON data to relevant fields.
public class AccountSale {
	
	public static class SaleLines {
		
		@JsonProperty("SaleLine")
		private Collection<SaleLine> saleLines;

		public Collection<SaleLine> getSaleLines() { return saleLines; }

		public void setSaleLines(Collection<SaleLine> saleLine) { this.saleLines = saleLine; }		
	}
	
	public static class SaleLine {
		@JsonProperty("itemID")
		private String itemId;
		@JsonProperty("unitPrice")
		private String unitPrice;
		@JsonProperty("unitQuantity")
		private String unitQuantity;
		@JsonProperty("timeStamp")
		private String timeStamp;
		@JsonProperty("Item")
		private SaleLineItem saleLineItem;
		
		public String getItemId() {	return itemId; }
		public void setItemId(String itemId) { this.itemId = itemId; }
		public String getUnitPrice() { return unitPrice; }
		public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
		public String getUnitQuantity() { return unitQuantity; }
		public void setUnitQuantity(String unitQuantity) { this.unitQuantity = unitQuantity; }
		public String getTimeStamp() { return timeStamp; }
		public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
		
		public SaleLineItem getSaleLineItem() { return saleLineItem; }
		public void setSaleLineItem(SaleLineItem saleLineItem) { this.saleLineItem = saleLineItem; }
		
	}
	
	
	public static class SaleLineItem {
		@JsonProperty("description")
		private String description;

		public String getDescription() { return description; }

		public void setDescription(String description) { this.description = description; }	
	}
	
	public static class SalePayments {
		@JsonProperty("SalePayment")
		private SalePayment salePayment;

		public SalePayment getSalePayment() { return salePayment; }

		public void setSalePayment(SalePayment salePayment) { this.salePayment = salePayment; }
	}
	
	public static class SalePayment {
		@JsonProperty("amount")
		private String amount;
		@JsonProperty("PaymentType")
		private SalePaymentType salePaymentType;
		
		public String getAmount() { return amount; }
		public void setAmount(String amount) { this.amount = amount; }
		public SalePaymentType getSalePaymentType() { return salePaymentType; }
		public void setSalePaymentType(SalePaymentType salePaymentType) { this.salePaymentType = salePaymentType; }
	}
	
	public static class SalePaymentType {
		String name;

		public String getName() { return name; }

		public void setName(String name) { this.name = name; }
	}
	
	
	public static class Sale {
		
		private String 	saleTotal, 
						calcSubtotal, 
						calcDiscount, 
						headerNk, 
						customer, 
						supplier, 
						organisation, 
						salesPerson, 
						campaign, 
						currency,
						purchasePriceWOVat, 
						purchaseCurrency, 
						pieceGood;
		
		private int salesValueWOVat;
		
		SaleLines sLs;
		
		SalePayments salePayments;
		
		@JsonCreator
		public Sale (@JsonProperty("total")String saleTotal,
				@JsonProperty("calcSubtotal")String calcSubtotal,
				@JsonProperty("calcDiscount")String calcDiscount,
				@JsonProperty("SaleLines")SaleLines sLs,
				@JsonProperty("SalePayments")SalePayments salePayments
		) {
			this.saleTotal = saleTotal;
			this.calcSubtotal = calcSubtotal;
			this.calcDiscount = calcDiscount;
			this.sLs = sLs;
			this.salePayments = salePayments;
			
			this.headerNk = "";
			this.customer = "";
			this.supplier = "";
			this.organisation = "";
			this.salesPerson = "";
			this.campaign = "";
			this.currency = "";
			this.purchasePriceWOVat = "";
			this.purchaseCurrency = "";
			this.pieceGood = "";
			
			setSalesValueWOVat();
		}

		public String getHeaderNk() { return headerNk; }

		public void setHeaderNk(String headerNk) { this.headerNk = headerNk; }

		public String getPieceGood() { return pieceGood; }

		public void setPieceGood(String pieceGood) { this.pieceGood = pieceGood; }

		public String getSaleTotal() { return saleTotal;}

		public void setSaleTotal(String saleTotal) { this.saleTotal = saleTotal; }

		public String getCalcSubtotal() { return calcSubtotal; }

		public void setCalcSubtotal(String calcSubtotal) { this.calcSubtotal = calcSubtotal; }

		public String getCalcDiscount() { return calcDiscount; }

		public void setCalcDiscount(String calcDiscount) { this.calcDiscount = calcDiscount; }

		public int getSalesValueWOVat() { return this.salesValueWOVat; }

		public void setSalesValueWOVat() {
			this.salesValueWOVat = Integer.parseInt(getCalcSubtotal()) - Integer.parseInt(getCalcDiscount());
		}
		
		public String getCustomer() { return customer; }
		public String getSupplier() { return supplier; }
		public String getOrganisation() { return organisation; }
		public String getSalesPerson() { return salesPerson; }
		public String getCampaign() { return campaign; }
		public String getCurrency() { return currency; }
		public String getPurchasePriceWOVat() { return purchasePriceWOVat; }		
		public String getPurchaseCurrency() { return purchaseCurrency; }
		
		public SaleLines getSLs() { return sLs; }

		public void setSLs(SaleLines sLs) { this.sLs = sLs; }

		public SalePayments getSalePayments() { return salePayments; }

		public void setSalePayments(SalePayments salePayments) { this.salePayments = salePayments; }
		
	}
}
