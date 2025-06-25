package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.BaseMasterDataItem;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseMasterDataRepository<T extends BaseMasterDataItem> {

    protected final DynamoDbTable<T> table;

    protected BaseMasterDataRepository(DynamoDbEnhancedClient enhancedClient, Class<T> clazz) {
        this.table = enhancedClient.table("MasterData", TableSchema.fromBean(clazz));
    }

    public T save(T item) {
        table.putItem(item);
        return item;
    }

    public Optional<T> findByPkAndEntityId(String pkValue, String entityIdValue) {
        Key key = Key.builder()
                .partitionValue(pkValue)
                .sortValue(entityIdValue)
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    public void deleteByPkAndEntityId(String pkValue, String entityIdValue) {
        Key key = Key.builder()
                .partitionValue(pkValue)
                .sortValue(entityIdValue)
                .build();
        table.deleteItem(key);
    }

    public PaginatedResult<T> findByPk(
            String index,
            String pkValue,
            Integer limit,
            Map<String, AttributeValue> exclusiveStartKey) {
        // Define query condition
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );
        // Define request
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(limit)
                .scanIndexForward(false);
        // Assign ExclusiveStartKey if exists
        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey); // Set the start key for pagination
        }
        // Build request
        QueryEnhancedRequest request = requestBuilder.build();

        // Dynamically select the target for the query (table or index)
        SdkIterable<Page<T>> pages;
        if (index != null && !index.isEmpty()) {
            pages = table.index(index).query(request);
        } else {
            pages = table.query(request);
        }

        return processFirstPage(pages);
    }

    public PaginatedResult<T> queryPaginatedProductByPKAndFilterByKeyword(
            String index,
            String pkValue,
            String keyword,
            Integer limit,
            Map<String, AttributeValue> exclusiveStartKey
    ) {
        // Define the query condition for the main table partition.
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(limit);

        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey);
        }

        // Build a FilterExpression if a keyword is provided.
        // This filters the results *after* they are read from the partition.
        if (keyword != null && !keyword.isBlank()) {
            Expression filterExpression = Expression.builder()
                    .expression("(contains(#name, :keyword) OR contains(#vendorId, :keyword))")
                    .putExpressionName("#name", "name")
                    .putExpressionName("#vendorId", "vendor_id")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .build();
            requestBuilder.filterExpression(filterExpression);
        }

        // Dynamically select the target for the query (table or index)
        SdkIterable<Page<T>> pages;
        if (index != null && !index.isEmpty()) {
            pages = table.index(index).query(requestBuilder.build());
        } else {
            pages = table.query(requestBuilder.build());
        }

        return processFirstPage(pages);
    }

    private PaginatedResult<T> processFirstPage(SdkIterable<Page<T>> pages) {
        // Initial as empty list
        List<T> pageItems = Collections.emptyList();
        Map<String, AttributeValue> pageLastEvaluatedKey = null;

        // Get the first page
        if (pages.iterator().hasNext()) {
            Page<T> firstPage = pages.iterator().next();
            // Get items from this single page
            pageItems = firstPage.items();
            // Get the last evaluated key from this page
            pageLastEvaluatedKey = firstPage.lastEvaluatedKey();
        }

        return PaginatedResult.<T>builder()
                .items(pageItems)
                .lastEvaluatedKey(pageLastEvaluatedKey)
                .build();
    }

    public List<T> queryAllProductByPKAndFilterByKeyword(
            String index,
            String pkValue,
            String keyword
    ) {
        // Define the query condition for the main table partition.
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional);

        // Build a FilterExpression if a keyword is provided.
        // This filters the results *after* they are read from the partition.
        if (keyword != null && !keyword.isBlank()) {
            Expression filterExpression = Expression.builder()
                    .expression("(contains(#name, :keyword) OR contains(#vendorId, :keyword))")
                    .putExpressionName("#name", "name")
                    .putExpressionName("#vendorId", "vendor_id")
                    .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
                    .build();
            requestBuilder.filterExpression(filterExpression);
        }

        // Dynamically select the target for the query (table or index)
        SdkIterable<Page<T>> pages;
        if (index != null && !index.isEmpty()) {
            pages = table.index(index).query(requestBuilder.build());
        } else {
            pages = table.query(requestBuilder.build());
        }

        return pages
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

}