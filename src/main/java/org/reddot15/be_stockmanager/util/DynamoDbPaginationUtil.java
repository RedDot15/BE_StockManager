package org.reddot15.be_stockmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reddot15.be_stockmanager.dto.response.pagination.DDBPageResponse;
import org.reddot15.be_stockmanager.entity.pagination.PaginatedResult;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class DynamoDbPaginationUtil {
    public static <T> DDBPageResponse<T> paginate(
            ObjectMapper objectMapper,
            Integer limit,
            String encodedNextPageToken,
            BiFunction<Integer, Map<String, AttributeValue>, PaginatedResult<T>> queryFunction) {

        // Default limit if not provided (defensive check, typically handled by service)
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<T> aggregatedItems = new ArrayList<>();
        Map<String, AttributeValue> currentExclusiveStartKey = null;
        boolean hasMore = true; // Initial assumption for the internal loop

        // Decode & Assign ExclusiveStartKey if exists using the utility
        currentExclusiveStartKey = PaginationTokenUtil.decodeNextPageToken(encodedNextPageToken, objectMapper);

        // Loop to aggregate items until limit is met or no more data from DynamoDB
        while (aggregatedItems.size() < limit && hasMore) {
            // Determine the limit for the *current* internal DynamoDB query
            int ddbQueryLimit = limit - aggregatedItems.size();
            // Cap ddbQueryLimit to DynamoDB's max internal limit
            if (ddbQueryLimit > 100) {
                ddbQueryLimit = 100;
            }

            // Query DynamoDB using the provided functional interface
            PaginatedResult<T> pageResult = queryFunction.apply(ddbQueryLimit, currentExclusiveStartKey);

            // Add all items retrieved from this single DynamoDB query
            aggregatedItems.addAll(pageResult.getItems());

            // Update currentExclusiveStartKey for the next potential iteration of the while loop
            currentExclusiveStartKey = pageResult.getLastEvaluatedKey();
            // 'hasMore' for the loop is true if DynamoDB returned a LastEvaluatedKey
            hasMore = currentExclusiveStartKey != null && !currentExclusiveStartKey.isEmpty();
        }

        // Encode LastEvaluatedKey for the response token based on the final currentExclusiveStartKey from the loop
        String newNextPageToken = PaginationTokenUtil.encodeLastEvaluatedKey(currentExclusiveStartKey, objectMapper);

        // Check if there are truly more pages using an additional "peek" query
        if (currentExclusiveStartKey != null &&
                queryFunction.apply(1, currentExclusiveStartKey).getLastEvaluatedKey() == null) {
            hasMore = false; // Override hasMore to false if the peek query yields no continuation key
            newNextPageToken = null; // Clear the next page token if there are no more pages
        }

        // Return the final PageResponse
        return DDBPageResponse.<T>builder()
                .items(aggregatedItems)
                .nextPageToken(newNextPageToken)
                .hasMore(hasMore)
                .build();
    }
}