package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.Product;
import org.reddot15.be_stockmanager.util.DynamoDbPaginationUtil;
import org.reddot15.be_stockmanager.util.QueryConditionalBuilder;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

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
        return table.index("pk-created_at-lsi")
                .query(QueryConditionalBuilder.build("Invoices", startDate, endDate))
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
                        findByPk(
                                "pk-created_at-lsi",
                                QueryConditional.keyEqualTo(Key.builder().partitionValue("Invoices").build()),
                                ddbQueryLimit,
                                currentExclusiveStartKey,
                                null)
        );
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        // Find Product by Partition Key "Invoices" and Sort Key is productId
        return findByPkAndEntityId("Invoices", invoiceId);
    }
}