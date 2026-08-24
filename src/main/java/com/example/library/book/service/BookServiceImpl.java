package com.example.library.book.service;

import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.mapper.BookMapper;
import com.example.library.common.dto.PageResponseDto;
import com.example.library.common.dto.PageResponseMapper;
import com.example.library.common.exception.DuplicateResourceException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.book.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookResponseDto createBook(BookRequestDto requestDto) {
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            throw new DuplicateResourceException("A book with this ISBN is already registered");
        }
        Book book = bookMapper.toEntity(requestDto);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponseDto(savedBook);
    }

    @Override
    @Transactional
    public BookResponseDto getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return bookMapper.toResponseDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookResponseDto> getAllBooks(int page, int size) {
        Page<BookResponseDto> result = bookRepository.findAll(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")))
                .map(bookMapper::toResponseDto);
        return PageResponseMapper.toPageResponse(result);
    }

    @Override
    public BookResponseDto updateBook(Long id, BookRequestDto requestDto) {
        Book book = findBookOrThrow(id);

        bookRepository.findByIsbn(requestDto.getIsbn())
                .filter(existingBook -> !existingBook.getId().equals(id))
                .ifPresent(existingBook -> {
                    throw new DuplicateResourceException("A book with this ISBN is already registered");
                });

        bookMapper.updateEntityFromDto(requestDto, book);
        Book updatedBook = bookRepository.save(book);
        return bookMapper.toResponseDto(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        bookRepository.delete(book);
    }

    @Override
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book with ID " + id + " was not found"
                        )
                );
    }

    @Override
    public void markAsBorrowed(Long id) {
        Book book = getBookEntityById(id);
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);
    }

    @Override
    public void markAsAvailable(Long id) {
        Book book = getBookEntityById(id);
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found"));
    }
}
