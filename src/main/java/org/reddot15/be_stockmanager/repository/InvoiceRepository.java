package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
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

    public List<Invoice> findInvoicesByCreatedAtBetween(String startDate, String endDate) {
        // Initialize variable
        QueryConditional queryConditional;
        boolean isStartDateEmpty = startDate == null || startDate.trim().isEmpty();
        boolean isEndDateEmpty = endDate == null || endDate.trim().isEmpty();

        if (isStartDateEmpty && isEndDateEmpty) {
            // Case 1: Both startDate and endDate are empty - find all records for the partition key
            queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue("Invoices").build()
            );
        } else if (isStartDateEmpty) {
            // Case 2: startDate is empty, endDate is not - find every record BEFORE endDate
            queryConditional = QueryConditional.sortLessThanOrEqualTo(
                    Key.builder().partitionValue("Invoices").sortValue(endDate).build()
            );
        } else if (isEndDateEmpty) {
            // Case 3: endDate is empty, startDate is not - find every record AFTER startDate
            queryConditional = QueryConditional.sortGreaterThanOrEqualTo(
                    Key.builder().partitionValue("Invoices").sortValue(startDate).build()
            );
        } else {
            // Case 4: Both startDate and endDate are present - find records BETWEEN startDate and endDate
            queryConditional = QueryConditional.sortBetween(
                    Key.builder().partitionValue("Invoices").sortValue(startDate).build(), // Lower bound
                    Key.builder().partitionValue("Invoices").sortValue(endDate).build()   // Upper bound
            );
        }

        return table.index("pk-created_at-lsi")
                .query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public DDBPageResponse<Invoice> findAllInvoices(Integer limit, String encodedNextPageToken) {
        // Delegate to the generic pagination utility
        return DynamoDbPaginationUtil.paginate(
                objectMapper,
                limit,
                encodedNextPageToken,
                // Provide the specific query function for Invoices
                (ddbQueryLimit, currentExclusiveStartKey) ->
                        findByPk("pk-created_at-lsi", "Invoices", ddbQueryLimit, currentExclusiveStartKey, null, false)
        );
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        // Find Product by Partition Key "Invoices" and Sort Key is productId
        return findByPkAndEntityId("Invoices", invoiceId);
    }
}