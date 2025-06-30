package org.reddot15.be_stockmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.reddot15.be_stockmanager.dto.response.InvoiceResponse;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.SaleItem;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import org.reddot15.be_stockmanager.mapper.InvoiceMapper;
import org.reddot15.be_stockmanager.repository.InvoiceRepository;
import org.reddot15.be_stockmanager.repository.ProductRepository;
import org.reddot15.be_stockmanager.util.CSVUtil;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
@Service
public class InvoiceService {
    InvoiceRepository invoiceRepository;
    InvoiceMapper invoiceMapper;
    ProductRepository productRepository;
    ObjectMapper objectMapper;

    @PreAuthorize("hasAuthority('IMPORT_INVOICES')")
    public List<InvoiceResponse> importInvoicesFromCSV(MultipartFile file) {
        List<Invoice> importedInvoices = CSVUtil.processCSVFile(file, this::processInvoiceRecord);

        // Convert to response DTOs
        return importedInvoices.stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Invoice processInvoiceRecord(CSVRecord csvRecord) {
        try {
            // Mapping
            Invoice invoice = invoiceMapper.toInvoice(csvRecord);
            // Checking if any sale-item not exists
            validateSaleItems(invoice.getSales());
            // Save the invoice
            return invoiceRepository.saveInvoice(invoice);
        } catch (IllegalArgumentException | DateTimeParseException | JsonProcessingException e) {
            // Log the error and wrap it in a custom, controlled exception.
            log.error("Error processing CSV record: {}. Details: {}", csvRecord.toString(), e.getMessage());
            throw new AppException(ErrorCode.INVALID_RECORD);
        }
    }

    private void validateSaleItems(List<SaleItem> saleItems) {
        saleItems.forEach(saleItem ->
                productRepository.findProductById(saleItem.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND))
        );
    }

    @PreAuthorize("hasAuthority('VIEW_INVOICES')")
    public DDBPageResponse<InvoiceResponse> getInvoices(Integer limit, String encodedNextPageToken) {
        // Delegate to the generic pagination utility with the chosen function.
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                encodedNextPageToken,
                limit,
                (ddbQueryLimit, currentExclusiveStartKey) ->
                        invoiceRepository.findOneInvoicesPage(
                                currentExclusiveStartKey,
                                ddbQueryLimit),
                invoiceMapper::toResponse
        );
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