package org.reddot15.be_stockmanager.dto.response.pagination;

import lombok.*;
import lombok.experimental.FieldDefaults;
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
        this.page = pageable.getPageNumber();
        this.size = pageable.getPageSize();
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil(totalElements / (double) size);
        this.items = items;
    }

    public static <T> PageResponse<T> empty() {
        return empty(Pageable.unpaged());
    }

    public static <T> PageResponse<T> empty(Pageable pageable) {
        return new PageResponse<>(Collections.emptyList(), pageable, 0L);
    }
}