package com.example.library.book.dto;

import org.springframework.core.io.Resource;

public record BookCoverDownloadDto(
        Resource resource,
        String contentType,
        String originalFileName
) {
}
