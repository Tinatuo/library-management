package com.example.library.book.service;

import com.example.library.book.dto.BookCoverDownloadDto;
import com.example.library.book.dto.BookRequestDto;
import com.example.library.book.dto.BookResponseDto;
import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.mapper.BookMapper;
import com.example.library.book.storage.BookCoverStorageService;
import com.example.library.common.aop.Audited;
import com.example.library.common.dto.PageResponseDto;
import com.example.library.common.dto.PageResponseMapper;
import com.example.library.common.exception.DuplicateResourceException;
import com.example.library.common.exception.InvalidFileException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.book.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private static final Set<String> ALLOWED_COVER_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final long MAX_COVER_SIZE_BYTES = 5L * 1024 * 1024;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookCoverStorageService coverStorageService;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper,
                           BookCoverStorageService coverStorageService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.coverStorageService = coverStorageService;
    }

    @Override
    @Audited(
            action = "BOOK_CREATE",
            details = "isbn=#{#requestDto.isbn}"
    )
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
    @Cacheable(value = "books", key = "#id")
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
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action="BOOK_UPDATE",
            details="bookId=#{#id}"
    )
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
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action = "BOOK_DELETE",
            details = "bookId=#{#id}"
    )
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getCoverStoredFileName() != null) {
            coverStorageService.delete(book.getCoverStoredFileName());
        }
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
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action="BOOK_BORROW",
            details="bookId=#{#id}"
    )
    public void markAsBorrowed(Long id) {
        Book book = getBookEntityById(id);
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);
    }

    @Override
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action="BOOK_RETURN",
            details="bookId=#{#id}"
    )
    public void markAsAvailable(Long id) {
        Book book = getBookEntityById(id);
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found"));
    }

    @Override
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action="BOOK_COVER_UPLOAD",
            details="bookId=#{#id}"
    )
    public BookResponseDto uploadCoverImage(Long id, MultipartFile file) {
        Book book = findBookOrThrow(id);
        validateCoverImage(file);


        if (book.getCoverStoredFileName() != null) {
            coverStorageService.delete(book.getCoverStoredFileName());
        }

        String storedFileName = coverStorageService.store(file);

        book.setCoverStoredFileName(storedFileName);
        book.setCoverOriginalFileName(StringUtils.cleanPath(file.getOriginalFilename()));
        book.setCoverContentType(file.getContentType());
        book.setCoverFileSize(file.getSize());

        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponseDto(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookCoverDownloadDto getCoverImage(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getCoverStoredFileName() == null) {
            throw new ResourceNotFoundException("Book with ID " + id + " has no cover image uploaded");
        }

        var resource = coverStorageService.loadAsResource(book.getCoverStoredFileName());
        return new BookCoverDownloadDto(resource, book.getCoverContentType(), book.getCoverOriginalFileName());
    }

    @Override
    @CacheEvict(value = "books", key = "#id")
    @Audited(
            action="BOOK_COVER_DELETE",
            details="bookId=#{#id}"
    )
    public void deleteCoverImage(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getCoverStoredFileName() == null) {
            return;
        }
        coverStorageService.delete(book.getCoverStoredFileName());
        book.setCoverStoredFileName(null);
        book.setCoverOriginalFileName(null);
        book.setCoverContentType(null);
        book.setCoverFileSize(null);
        bookRepository.save(book);
    }

    private void validateCoverImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Cover image file must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_COVER_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG, and WEBP images are allowed for book covers");
        }
        if (file.getSize() > MAX_COVER_SIZE_BYTES) {
            throw new InvalidFileException("Cover image size must not exceed 5MB");
        }
    }
}
