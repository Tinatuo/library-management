package com.example.library.book.service;

import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;
import com.example.library.common.dto.PageResponseDto;
import com.example.library.book.dto.BookCoverDownloadDto;
import org.springframework.web.multipart.MultipartFile;

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

    BookResponseDto uploadCoverImage(Long id, MultipartFile file);

    BookCoverDownloadDto getCoverImage(Long id);

    void deleteCoverImage(Long id);
}
