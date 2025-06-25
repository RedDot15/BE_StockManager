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
            Map<String, AttributeValue> exclusiveStartKey,
            Expression filterExpression,
            boolean fetchAllPages) {
        // Define query condition
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );
        // Define request
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(false);
        // Assign limit if provided
        if (limit != null && limit > 0) {
            requestBuilder.limit(limit);
        }
        // Assign ExclusiveStartKey if exists
        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey); // Set the start key for pagination
        }
        // Apply the filter expression if provided
        if (filterExpression != null) {
            requestBuilder.filterExpression(filterExpression);
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

        if (!fetchAllPages)
            return processFirstPage(pages);
        else
            return processAllPage(pages);
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

    private PaginatedResult<T> processAllPage(SdkIterable<Page<T>> pages) {
        // Initial as empty list
        List<T> pageItems = Collections.emptyList();

        // Get all items
        if (pages.iterator().hasNext()) {
            pageItems = pages
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
        }

        return PaginatedResult.<T>builder()
                .items(pageItems)
                .lastEvaluatedKey(null)
                .build();
    }
}