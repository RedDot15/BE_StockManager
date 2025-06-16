package org.reddot15.be_stockmanager.repository;

import org.reddot15.be_stockmanager.model.BaseMasterDataItem;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
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

    public List<T> findByPk(String pkValue) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(pkValue).build()
        );
        return table.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

}