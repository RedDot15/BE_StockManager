package org.reddot15.be_stockmanager.converter.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.stream.Collectors;

public class AttributeValueConverter {
    // Helper method to convert Map<String, String> (from decoded JSON) to Map<String, AttributeValue>
    public static Map<String, AttributeValue> convertMapStringToAttributeValue(Map<String, String> stringMap) {
        if (stringMap == null) return null;
        return stringMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> AttributeValue.builder().s(entry.getValue()).build() // Assuming all keys are Strings in PK/SK

                ));
    }

    // Helper method to convert Map<String, AttributeValue> to Map<String, String> for JSON serialization
    public static Map<String, String> convertMapAttributeValueToString(Map<String, AttributeValue> attributeValueMap) {
        if (attributeValueMap == null) return null;
        return attributeValueMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().s() // Assuming the relevant parts of LastEvaluatedKey are Strings
                ));
    }
}
