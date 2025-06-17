package org.reddot15.be_stockmanager.dto.response.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PageResponse<T> {
    private List<T> items;
    private String nextPageToken; // Base64 encoded lastEvaluatedKey
    private boolean hasMore; // Convenience flag for client
}