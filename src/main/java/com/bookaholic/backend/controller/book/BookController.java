package com.bookaholic.backend.controller.book;

import com.bookaholic.backend.DTO.books.BookDetailsResponse;
import com.bookaholic.backend.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// Book Controller 

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService service;

    // Admin - Endpoint to add a book with an image
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDetailsResponse> addBook(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("isbn") String isbn,
            @RequestParam("copies") Integer copies,
            @RequestParam("image") MultipartFile image) throws IOException {

        log.info("Adding book with title: {}, author: {}, isbn: {}", title, author, isbn);
        return ResponseEntity.ok(service.addBook(title, author, isbn, copies, image));
    }

    // Admin - Endpoint to update a book (image is optional)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDetailsResponse> updateBook(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("isbn") String isbn,
            @RequestParam("totalCopies") Integer totalCopies,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        log.info("Updating book with id: {}, title: {}", id, title);
        return ResponseEntity.ok(service.updateBook(id, title, author, isbn, totalCopies, image));
    }

    // Public/User endpoint to view all books
    @GetMapping
    public ResponseEntity<List<BookDetailsResponse>> getAllBooks() {
        log.info("Getting all books");
        return ResponseEntity.ok(service.getAllBooks());
    }

    // Public/User endpoint to view a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsResponse> getBookById(@PathVariable Long id) {
        log.info("Getting book with id: {}", id);
        return ResponseEntity.ok(service.getBookById(id));
    }

}
