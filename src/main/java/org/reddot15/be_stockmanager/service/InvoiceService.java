package org.reddot15.be_stockmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.reddot15.be_stockmanager.dto.response.InvoiceResponse;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.InvoiceMapper;
import org.reddot15.be_stockmanager.repository.InvoiceRepository;
import org.reddot15.be_stockmanager.util.DateTimeValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class InvoiceService {
    InvoiceRepository invoiceRepository;
    ObjectMapper objectMapper;
    InvoiceMapper invoiceMapper;

    @PreAuthorize("hasAuthority('MANAGE_INVOICES')")
    public List<Invoice> importInvoicesFromCSV(MultipartFile file) {
        // File empty exception
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_FILE);
        }

        // Initialize result variable
        List<Invoice> importedInvoices = new ArrayList<>();
        // Parse file
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            // Get records
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                try {
                    Invoice invoice = new Invoice();
                    // Set Partition Key
                    invoice.setPk("Invoices");
                    // Generate a new UUID for entityId (Sort Key)
                    invoice.setEntityId(UUID.randomUUID().toString());

                    // Map CSV columns to Invoice fields
                    invoice.setCreatedAt(DateTimeValidator.validate(csvRecord.get("created_at")));
                    invoice.setUpdatedAt(DateTimeValidator.validate(csvRecord.get("updated_at")));
                    invoice.setTotal(Double.parseDouble(csvRecord.get("total")));
                    invoice.setTax(Double.parseDouble(csvRecord.get("tax")));

                    // Handle SaleItems - parse the JSON array from the 'sales' column
                    List<SaleItem> saleItems = parseSaleItemsJson(csvRecord.get("sales"));
                    invoice.setSales(saleItems);

                    // Save the invoice
                    importedInvoices.add(invoiceRepository.saveInvoice(invoice));
                } catch (IllegalArgumentException | DateTimeParseException | JsonProcessingException e) {
                    // Invalid record exception
                    throw new AppException(ErrorCode.INVALID_RECORD);
                }
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PARSE_FAILED);
        }

        return importedInvoices;
    }

    @PreAuthorize("hasAuthority('MANAGE_INVOICES')")
    public DDBPageResponse<InvoiceResponse> getAll(Integer limit, String nextPageToken) {
        // Business logic or defaulting of limit remains here
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // Get invoices
        DDBPageResponse<Invoice> invoicePage = invoiceRepository.findAllInvoices(limit, nextPageToken);

        // Map the entities from the repository to DTOs for the API response
        List<InvoiceResponse> invoiceResponses = invoicePage.getItems().stream()
                .map(invoiceMapper::toResponse)
                .toList();

        // Return the paginated response with DTOs
        return DDBPageResponse.<InvoiceResponse>builder()
                .items(invoiceResponses)
                .nextPageToken(invoicePage.getNextPageToken()) // Get the token from the repository's result
                .hasMore(invoicePage.isHasMore())             // Get the hasMore flag from the repository's result
                .build();
    }

    @PreAuthorize("hasAuthority('MANAGE_INVOICES')")
    public InvoiceResponse getById(String invoiceId) {
        // Get invoice
        Invoice entity = invoiceRepository.findInvoiceById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        // Return
        return invoiceMapper.toResponse(entity);
    }

    private List<SaleItem> parseSaleItemsJson(String salesJsonString) throws JsonProcessingException {
        // Null exception
        if (salesJsonString == null || salesJsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Deserialize the JSON string into a List<SaleItem>
        return objectMapper.readValue(salesJsonString, new TypeReference<List<SaleItem>>() {});
    }
}