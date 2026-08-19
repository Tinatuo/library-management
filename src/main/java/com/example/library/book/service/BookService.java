package com.example.library.book.service;

import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;

import java.util.List;

public interface BookService {


    BookResponseDto createBook(BookRequestDto requestDto);

    BookResponseDto getBookById(Long id);

    List<BookResponseDto> getAllBooks();

    BookResponseDto updateBook(Long id, BookRequestDto requestDto);

    void deleteBook(Long id);

    Book getBookEntityById(Long id);

    void markAsBorrowed(Long id);

    void markAsAvailable(Long id);
}
