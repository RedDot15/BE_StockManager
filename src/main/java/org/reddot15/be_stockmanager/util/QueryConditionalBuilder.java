package org.reddot15.be_stockmanager.util;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

public class QueryConditionalBuilder {
    public static <T> QueryConditional build(String sk, Double startSK, Double endSK) {
        boolean isStartSKEmpty = startSK == null;
        boolean isEndSKEmpty = endSK == null ;

        QueryConditional queryConditional;
        if (isStartSKEmpty && isEndSKEmpty) {
            // Case 1: Both minPrice and maxPrice are empty - find all records for the partition key
            queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(sk).build()
            );
        } else if (isStartSKEmpty) {
            // Case 2: startDate is empty, endDate is not - find every record BEFORE endDate
            queryConditional = QueryConditional.sortLessThanOrEqualTo(
                    Key.builder().partitionValue(sk).sortValue(endSK).build()
            );
        } else if (isEndSKEmpty) {
            // Case 3: endDate is empty, startDate is not - find every record AFTER startDate
            queryConditional = QueryConditional.sortGreaterThanOrEqualTo(
                    Key.builder().partitionValue(sk).sortValue(startSK).build()
            );
        } else {
            // Case 4: Both startDate and endDate are present - find records BETWEEN startDate and endDate
            queryConditional = QueryConditional.sortBetween(
                    Key.builder().partitionValue(sk).sortValue(startSK).build(), // Lower bound
                    Key.builder().partitionValue(sk).sortValue(endSK).build()   // Upper bound
            );
        }

        return queryConditional;
    }

    public static QueryConditional build(String sk, String startSK, String endSK) {
        boolean isStartSKEmpty = startSK == null;
        boolean isEndSKEmpty = endSK == null ;

        QueryConditional queryConditional;
        if (isStartSKEmpty && isEndSKEmpty) {
            // Case 1: Both minPrice and maxPrice are empty - find all records for the partition key
            queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(sk).build()
            );
        } else if (isStartSKEmpty) {
            // Case 2: startDate is empty, endDate is not - find every record BEFORE endDate
            queryConditional = QueryConditional.sortLessThanOrEqualTo(
                    Key.builder().partitionValue(sk).sortValue(endSK).build()
            );
        } else if (isEndSKEmpty) {
            // Case 3: endDate is empty, startDate is not - find every record AFTER startDate
            queryConditional = QueryConditional.sortGreaterThanOrEqualTo(
                    Key.builder().partitionValue(sk).sortValue(startSK).build()
            );
        } else {
            // Case 4: Both startDate and endDate are present - find records BETWEEN startDate and endDate
            queryConditional = QueryConditional.sortBetween(
                    Key.builder().partitionValue(sk).sortValue(startSK).build(), // Lower bound
                    Key.builder().partitionValue(sk).sortValue(endSK).build()   // Upper bound
            );
        }

        return queryConditional;
    }
}

// TODO: Remove duplication