package com.zoined.lightspeedpos.modules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//POJO class for Item module. Maps the JSON data to relevant fields.
public class AccountItem {
	
	public static class Note {
		String note;
		
		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}
		@Override
		public String toString() {
			return note;
		}
	}
    
	public static class Category {
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}	
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static class CustomFieldValue {
		private String name;	
		private String value;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
	public static class CustomFieldValues {
		@JsonProperty("CustomFieldValue")
		CustomFieldValue customFieldValue;

		public CustomFieldValue getCustomFieldValue() {
			return customFieldValue;
		}

		public void setCustomFieldValue(CustomFieldValue customFieldValue) {
			this.customFieldValue = customFieldValue;
		}
		
	}
	
	@JsonPropertyOrder({"systemSku", "description", "Note" ,"Category", "avgCost", "CustomFieldValues", "itemType"})
	public static class Item {
		
		private String 	systemSku,
						description,
						avgCost,
						itemType, 
						lastPrice,
						suppliers,
						productBrands,
						vatRate,
						unitOfMeasure,
						size;
		
		Category category;
		
		Note note;
		
		CustomFieldValues customFieldValues;

		@JsonCreator
		public Item(@JsonProperty("systemSku") String systemSku, 
				@JsonProperty("description") String description,
				@JsonProperty("avgCost") String avgCost,
				@JsonProperty("itemType") String itemType,
				@JsonProperty("Note") Note note,
				@JsonProperty("Category") Category category,
				@JsonProperty("CustomFieldValues") CustomFieldValues customFieldValues) {
			this.systemSku = systemSku;
			this.description = description;
			this.avgCost = avgCost;
			this.itemType = itemType;
			this.note = note;
			this.category = category;
			this.customFieldValues = customFieldValues;
			this.lastPrice = "";
			this.suppliers = "";
			this.productBrands = "";
			this.vatRate = "";
			this.unitOfMeasure = "";
			this.size = "";
		}
		
		public Note getNote() {	return note; }
		
		public void setNote(Note note) { this.note = note; }
		
		public Category getCategory() { return category; }
		
		public void setCategory(Category category) { this.category = category; }
		
		public CustomFieldValues getCustomFieldValues() { return customFieldValues; }
		
		public void setCustomFieldValues(CustomFieldValues customFieldValues) { this.customFieldValues = customFieldValues; } 
		
		public String getSystemSku() { 	return systemSku; }
		public void setSystemSku(String systemSku) { this.systemSku = systemSku; }
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public String getAvgCost() { return avgCost; }
		public void setAvgCost(String avgCost) { this.avgCost = avgCost; }
		public String getItemType() { return itemType; }
		public void setItemType(String itemType) { this.itemType = itemType; }
			
		public String getLastPrice() { return lastPrice; }
		public String getSuppliers() { return suppliers; }
		public String getProductBrands() { return productBrands; }
		public String getVatRate() { return vatRate; }
		public String getUnitOfMeasure() { return unitOfMeasure; }
		public String getSize() { return size; }
		
	}

}
