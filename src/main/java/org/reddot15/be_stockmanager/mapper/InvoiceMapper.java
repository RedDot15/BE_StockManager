package org.reddot15.be_stockmanager.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVRecord;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.reddot15.be_stockmanager.dto.response.InvoiceResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.util.TimeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvoiceMapper {
    // Response
    InvoiceResponse toResponse(Invoice entity);

    default Invoice toInvoice(CSVRecord csvRecord) throws JsonProcessingException {
        return Invoice.builder()
                .pk("Invoices")
                .entityId(UUID.randomUUID().toString())
                .createdAt(TimeValidator.validateDateTime(csvRecord.get("created_at")))
                .updatedAt(TimeValidator.validateDateTime(csvRecord.get("updated_at")))
                .total(Double.parseDouble(csvRecord.get("total")))
                .tax(Double.parseDouble(csvRecord.get("tax")))
                .sales(parseSaleItemsJson(csvRecord.get("sales")))
                .build();
    };

    private List<SaleItem> parseSaleItemsJson(String salesJsonString) throws JsonProcessingException {
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
