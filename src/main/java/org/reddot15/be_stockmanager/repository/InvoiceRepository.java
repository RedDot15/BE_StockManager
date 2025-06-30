package org.reddot15.be_stockmanager.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.reddot15.be_stockmanager.entity.Invoice;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import org.reddot15.be_stockmanager.util.QueryConditionalBuilder;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Repository
public class InvoiceRepository extends BaseMasterDataRepository<Invoice> {

    public InvoiceRepository(DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        super(enhancedClient, Invoice.class);
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Assign Partition Key as "Invoices"
        invoice.setPk("Invoices");
        return save(invoice);
    }

    public PaginatedResult<Invoice> findOneInvoicesPage(
            Map<String, AttributeValue> nextPageToken,
            Integer limit) {
        // Return
        return findOnePage(
                "pk-created_at-lsi",
                QueryConditional.keyEqualTo(Key.builder().partitionValue("Invoices").build()),
                null,
                nextPageToken,
                limit);
    }

    public List<Invoice> findInvoicesByCreatedAtBetween(String startDate, String endDate) {
        return table.index("pk-created_at-lsi")
                .query(QueryConditionalBuilder.build("Invoices", startDate, endDate))
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }


    public Optional<Invoice> findInvoiceById(String invoiceId) {
        // Find Product by Partition Key "Invoices" and Sort Key is productId
        return findByPkAndEntityId("Invoices", invoiceId);
    }
}