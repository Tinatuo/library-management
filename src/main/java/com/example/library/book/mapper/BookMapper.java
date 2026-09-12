package com.example.library.book.mapper;

import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(BookRequestDto dto) {
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .publisher(dto.getPublisher())
                .publishedYear(dto.getPublishedYear())
                .status(BookStatus.AVAILABLE)
                .build();
    }

    public void updateEntityFromDto(BookRequestDto dto, Book book) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublishedYear(dto.getPublishedYear());
    }

    public BookResponseDto toResponseDto(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishedYear(book.getPublishedYear())
                .status(book.getStatus())
                .hasCoverImage(book.getCoverStoredFileName() != null)
                .coverOriginalFileName(book.getCoverOriginalFileName())
                .coverContentType(book.getCoverContentType())
                .coverFileSize(book.getCoverFileSize())
                .build();
    }
}

