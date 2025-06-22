package org.reddot15.be_stockmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVRecord;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.SaleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CSVUtil {
    public static Product createProductFromCsvRecord(CSVRecord csvRecord) {
        return Product.builder()
                .pk("Products")
                .entityId(csvRecord.get("entity_id"))
                .vendorId(csvRecord.get("vendor_id"))
                .name(csvRecord.get("name"))
                .categoryName(csvRecord.get("category_name"))
                .importPrice(Double.parseDouble(csvRecord.get("import_price")))
                .salePrice(Double.parseDouble(csvRecord.get("sale_price")))
                .amount(Integer.parseInt(csvRecord.get("amount")))
                .earliestExpiry(TimeValidator.validateDate(csvRecord.get("earliest_expiry")))
                .vat(Double.parseDouble(csvRecord.get("vat")))
                .build();
    }

    public static Invoice mapCsvRecordToInvoice(CSVRecord csvRecord) throws JsonProcessingException {
        Invoice invoice = new Invoice();
        // Set Partition Key
        invoice.setPk("Invoices");
        // Generate a new UUID for entityId (Sort Key)
        invoice.setEntityId(UUID.randomUUID().toString());
        // Map CSV columns to Invoice fields
        invoice.setCreatedAt(TimeValidator.validateDateTime(csvRecord.get("created_at")));
        invoice.setUpdatedAt(TimeValidator.validateDateTime(csvRecord.get("updated_at")));
        invoice.setTotal(Double.parseDouble(csvRecord.get("total")));
        invoice.setTax(Double.parseDouble(csvRecord.get("tax")));
        // Handle SaleItems - parse the JSON array
        invoice.setSales(parseSaleItemsJson(csvRecord.get("sales")));
        return invoice;
    }

    private static List<SaleItem> parseSaleItemsJson(String salesJsonString) throws JsonProcessingException {
        // Null exception
        if (salesJsonString == null || salesJsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // New object mapper
        ObjectMapper objectMapper = new ObjectMapper();
        // Deserialize the JSON string into a List<SaleItem>
        return objectMapper.readValue(salesJsonString, new TypeReference<List<SaleItem>>() {});
    }
}
