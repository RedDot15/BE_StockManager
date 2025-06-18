package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.reddot15.be_stockmanager.util.PaginationTokenUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Repository
public class InvoiceRepository extends BaseMasterDataRepository<Invoice> {
    ObjectMapper objectMapper;

    public InvoiceRepository(DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        super(enhancedClient, Invoice.class);
        this.objectMapper = objectMapper;
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Assign Partition Key as "Invoices"
        invoice.setPk("Invoices");
        return save(invoice);
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        // Find Permission by Partition Key "Permissions" and Sort Key is permissionId
        return findByPkAndEntityId("Invoices", invoiceId);
    }

    public void deleteInvoiceById(String invoiceId) {
        // Delete Invoice by Partition Key "Invoices" and Sort Key is invoiceId
        deleteByPkAndEntityId("Invoices", invoiceId);
    }

    public List<Invoice> findInvoicesByCreatedAtBetween(String startDate, String endDate) {
        // Query LSI
        QueryConditional queryConditional = QueryConditional.sortBetween(
                Key.builder()
                        .partitionValue("Invoices")
                        .sortValue(startDate)
                        .build(),
                Key.builder()
                        .partitionValue("Invoices")
                        .sortValue(endDate)
                        .build()
        );

        return table.index("pk-created_at-lsi")
                .query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public PageResponse<Invoice> findAllInvoices(Integer limit, String encodedNextPageToken) {
        // Delegate to the generic pagination utility
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                // Provide the specific query function for Invoices
                (ddbQueryLimit, currentExclusiveStartKey) ->
                        findByPk("Invoices", ddbQueryLimit, currentExclusiveStartKey)
        );
    }
}