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
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.InvoiceMapper;
import org.reddot15.be_stockmanager.repository.InvoiceRepository;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.util.CSVUtil;
import org.reddot15.be_stockmanager.util.TimeValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    InvoiceMapper invoiceMapper;
    ProductRepository productRepository;

    @PreAuthorize("hasAuthority('IMPORT_INVOICES')")
    public List<InvoiceResponse> importInvoicesFromCSV(MultipartFile file) {
        // File empty exception
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_FILE);
        }

        // Initialize result variable
        List<Invoice> importedInvoices = new ArrayList<>();
        // Parse file
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            // For each record
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                try {
                    // Mapping
                    Invoice invoice = CSVUtil.mapCsvRecordToInvoice(csvRecord);
                    // Checking if any sale-item not exists
                    validateSaleItems(invoice.getSales());
                    // Save the invoice
                    importedInvoices.add(invoiceRepository.saveInvoice(invoice));
                } catch (IllegalArgumentException | DateTimeParseException | JsonProcessingException e) {
                    // Log the error for debugging purposes, but rethrow as a controlled exception
                    log.error("Error processing CSV record: {}", csvRecord.toString(), e);
                    throw new AppException(ErrorCode.INVALID_RECORD);
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_PARSE_FAILED);
        }

        return importedInvoices.stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    private void validateSaleItems(List<SaleItem> saleItems) {
        saleItems.forEach(saleItem ->
                productRepository.findProductById(saleItem.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND))
        );
    }

    @PreAuthorize("hasAuthority('VIEW_INVOICES')")
    public DDBPageResponse<InvoiceResponse> getAll(Integer limit, String nextPageToken) {
        // Business logic or defaulting of limit remains here
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // Get invoices
        DDBPageResponse<Invoice> invoicePage = invoiceRepository.findAllInvoices(limit, nextPageToken);

        // Map the entities from the repository to DTOs for the API response
        List<InvoiceResponse> invoiceResponses = invoicePage.getItems()
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();

        // Return the paginated response with DTOs
        return DDBPageResponse.<InvoiceResponse>builder()
                .items(invoiceResponses)
                .nextPageToken(invoicePage.getNextPageToken()) // Get the token from the repository's result
                .hasMore(invoicePage.isHasMore())             // Get the hasMore flag from the repository's result
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_INVOICES')")
    public InvoiceResponse getById(String invoiceId) {
        // Get invoice
        Invoice entity = invoiceRepository.findInvoiceById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        // Return
        return invoiceMapper.toResponse(entity);
    }

}