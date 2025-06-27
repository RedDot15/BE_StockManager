package org.reddot15.be_stockmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reddot15.be_stockmanager.exception.AppException;
import org.reddot15.be_stockmanager.exception.ErrorCode;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public class PaginationTokenUtil {

    private PaginationTokenUtil() {
        // Private constructor to prevent instantiation
    }

    public static Map<String, AttributeValue> decodeNextPageToken(String nextPageToken, ObjectMapper objectMapper) {
        if (nextPageToken == null || nextPageToken.trim().isEmpty()) {
            return null;
        }
        try {
            String decodedString = new String(Base64.getUrlDecoder().decode(nextPageToken));
            Map<String, String> stringMap = objectMapper.readValue(decodedString, Map.class);
            return convertMapStringToAttributeValue(stringMap);
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_PAGINATION_TOKEN);
        } catch (IllegalArgumentException e) { // For invalid Base64 string
            throw new AppException(ErrorCode.INVALID_PAGINATION_TOKEN);
        }
    }

    public static String encodeLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey, ObjectMapper objectMapper) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }
        try {
            Map<String, String> stringMap = convertMapAttributeValueToString(lastEvaluatedKey);
            String jsonString = objectMapper.writeValueAsString(stringMap);
            return Base64.getUrlEncoder().encodeToString(jsonString.getBytes());
        } catch (IOException e) {
            throw new AppException(ErrorCode.SERIALIZE_PAGINATION_TOKEN_FAILED);
        }
    }

    private static Map<String, AttributeValue> convertMapStringToAttributeValue(Map<String, String> stringMap) {
        if (stringMap == null) return null;
        return stringMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            String value = entry.getValue();
                            // Attempt to parse as a number
                            try {
                                new BigDecimal(value);
                                return AttributeValue.builder().n(value).build();
                            } catch (NumberFormatException e) {
                                // If it's not a valid number, treat it as a string
                                return AttributeValue.builder().s(value).build();
                            }
                        }
                ));
    }

    private static Map<String, String> convertMapAttributeValueToString(Map<String, AttributeValue> attributeValueMap) {
        if (attributeValueMap == null) return null;
        return attributeValueMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            AttributeValue attributeValue = entry.getValue();
                            if (attributeValue.s() != null) {
                                return attributeValue.s();
                            } else if (attributeValue.n() != null) {
                                // DynamoDB stores numbers as strings, so .n() returns a String
                                return attributeValue.n();
                            } else if (attributeValue.bool() != null) {
                                // Handle boolean as string (e.g., "true" or "false")
                                return String.valueOf(attributeValue.bool());
                            }
                            // Unsupported exception
                            throw new AppException(ErrorCode.UNSUPPORT_DYNAMODB_TYPE);
                        }
                ));
    }
}