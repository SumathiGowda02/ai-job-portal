package com.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A plain, cache-friendly stand-in for Spring Data's Page<T>.
 *
 * Page/PageImpl carry internal Spring Data machinery (Pageable, Sort, etc.)
 * that Jackson cannot reliably round-trip through JSON — serializing one
 * to Redis and reading it back throws (first a ClassCastException, then a
 * LazyInitializationException, then a Sort deserialization error, each a
 * different symptom of the same root problem). A plain data class with no
 * framework internals has none of that: it serializes and deserializes as
 * ordinary JSON every time. The frontend only ever reads content/totalPages/
 * totalElements/number/size, so the JSON shape it receives is unchanged.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResult<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> PagedResult<T> from(Page<T> page) {
        return new PagedResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}