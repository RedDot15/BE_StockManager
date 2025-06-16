package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.model.Invoice;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InvoiceRepository extends BaseMasterDataRepository<Invoice> {

    public InvoiceRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, Invoice.class);
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

    public List<Invoice> findAllInvoices() {
        // Get all Invoices by Partition Key "Invoices"
        return findByPk("Invoices");
    }
}