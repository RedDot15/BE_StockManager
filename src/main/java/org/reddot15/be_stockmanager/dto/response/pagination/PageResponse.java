package org.reddot15.be_stockmanager.dto.response.pagination;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PageResponse<T> {
    List<T> items;

    int page;

    int size;

    long totalElements;

    int totalPages;

    public PageResponse(List<T> items, Pageable pageable, long totalElements) {
        // Handle unpaged scenario
        if (pageable.isUnpaged()) {
            this.page = 0; // Default to first page
            this.size = items.size(); // Size is the number of items if unpaged
            this.totalElements = totalElements;
            this.totalPages = totalElements > 0 ? 1 : 0; // One page if elements exist, else zero
        } else {
            this.page = pageable.getPageNumber();
            this.size = pageable.getPageSize();
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil(totalElements / (double) size);
        }
        this.items = items;
    }

    public static <T> PageResponse<T> empty() {
        return empty(Pageable.unpaged());
    }

    public static <T> PageResponse<T> empty(Pageable pageable) {
        return new PageResponse<>(Collections.emptyList(), pageable, 0L);
    }
}