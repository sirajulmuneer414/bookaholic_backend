package com.bookaholic.backend.controller.book;

import com.bookaholic.backend.DTO.books.BookDetailsResponse;
import com.bookaholic.backend.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService service;

    // Endpoint for ADMIN to add a book with an image
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDetailsResponse> addBook(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("isbn") String isbn,
            @RequestParam("copies") Integer copies,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        log.info("Adding book with title: {}, author: {}, isbn: {}", title, author, isbn);
        return ResponseEntity.ok(service.addBook(title, author, isbn, copies, image));
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