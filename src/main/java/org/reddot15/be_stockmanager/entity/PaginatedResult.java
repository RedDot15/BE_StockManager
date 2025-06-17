package org.reddot15.be_stockmanager.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class PaginatedResult<T> {
    private List<T> items;
    private Map<String, AttributeValue> lastEvaluatedKey;
}