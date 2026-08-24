package com.example.library.common.dto;

import org.springframework.data.domain.Page;

public final class PageResponseMapper {

    private PageResponseMapper() {
    }

    public static <T> PageResponseDto<T> toPageResponse(Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
