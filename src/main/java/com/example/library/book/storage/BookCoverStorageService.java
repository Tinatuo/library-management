package com.example.library.book.storage;

import com.example.library.common.exception.FileStorageException;
import com.example.library.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;


@Service
public class BookCoverStorageService {

    private final Path storageDirectory;

    public BookCoverStorageService(@Value("${app.storage.book-covers-dir}") String storageDir) {
        this.storageDirectory = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new FileStorageException("Could not create the book covers storage directory", e);
        }
    }


    public String store(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = storageDirectory.resolve(storedFileName).normalize();


        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(targetPath,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store cover image file", e);
        }

        return storedFileName;
    }


    public Resource loadAsResource(String storedFileName) {
        try {
            Path filePath = storageDirectory.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Cover image file is missing on disk: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("Invalid storage path for cover image: " + storedFileName, e);
        }
    }

    public void delete(String storedFileName) {
        try {
            Path filePath = storageDirectory.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete cover image file: " + storedFileName, e);
        }
    }

    private String extractExtension(String originalFileName) {
        String cleanName = StringUtils.cleanPath(
                originalFileName == null ? "" : originalFileName);
        int dotIndex = cleanName.lastIndexOf('.');
        return (dotIndex >= 0) ? cleanName.substring(dotIndex) : "";
    }
}
