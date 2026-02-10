package com.bookaholic.backend.DTO.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper for all list endpoints
 * 
 * @param <T> The type of content being paginated
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}
