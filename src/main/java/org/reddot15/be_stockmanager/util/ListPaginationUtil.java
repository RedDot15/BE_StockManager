package org.reddot15.be_stockmanager.util;

import org.reddot15.be_stockmanager.dto.response.pagination.PageResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class ListPaginationUtil {
    // Generic method to handle pagination
    public static <T> PageResponse<T> paginateList(List<T> allItems, Integer pageNumber, Integer pageSize) {
        int totalSize = allItems.size();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalSize);

        if (startIndex < totalSize) {
            return new PageResponse<>(
                    allItems.subList(startIndex, endIndex),
                    PageRequest.of(pageNumber, pageSize),
                    totalSize);
        } else {
            return PageResponse.empty();
        }
    }
}
