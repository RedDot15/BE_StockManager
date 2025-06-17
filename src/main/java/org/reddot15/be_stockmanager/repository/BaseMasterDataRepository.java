package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.entity.BaseMasterDataItem;
import org.reddot15.be_stockmanager.entity.PaginatedResult;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public PaginatedResult<T> findByPk(String pkValue, Integer limit, Map<String, AttributeValue> exclusiveStartKey) {
        // Define query condition
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );
        // Define request
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(limit);
        // Assign ExclusiveStartKey if exists
        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey); // Set the start key for pagination
        }
        // Build request
        QueryEnhancedRequest request = requestBuilder.build();

        // Execute the query ONCE to get the iterable of pages
        SdkIterable<Page<T>> pages = table.query(request);

        List<T> pageItems = null;
        Map<String, AttributeValue> pageLastEvaluatedKey = null;

        // Get the first page
        if (pages.iterator().hasNext()) {
            Page<T> firstPage = pages.iterator().next();
            // Get items from this single page
            pageItems = firstPage.items();
            // Get the last evaluated key from this page
            pageLastEvaluatedKey = firstPage.lastEvaluatedKey();
        } else {
            // Handle case where no items are returned (e.g., partition key not found)
            pageItems = Collections.emptyList();
        }

        return PaginatedResult.<T>builder()
                .items(pageItems)
                .lastEvaluatedKey(pageLastEvaluatedKey)
                .build();
    }

}