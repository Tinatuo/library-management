package com.example.library.book.service;

import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;
import com.example.library.common.dto.PageResponseDto;

import java.util.List;

public interface BookService {


    BookResponseDto createBook(BookRequestDto requestDto);

    BookResponseDto getBookById(Long id);

    PageResponseDto<BookResponseDto> getAllBooks(int page, int size);

    BookResponseDto updateBook(Long id, BookRequestDto requestDto);

    void deleteBook(Long id);

    Book getBookEntityById(Long id);

    void markAsBorrowed(Long id);

    void markAsAvailable(Long id);
}
