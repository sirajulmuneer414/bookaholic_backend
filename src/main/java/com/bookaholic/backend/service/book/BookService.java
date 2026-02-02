package com.bookaholic.backend.service.book;

import com.bookaholic.backend.DTO.books.BookDetailsResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

    // Book service interface

public interface BookService {
    @Transactional
    BookDetailsResponse addBook(String title, String author, String isbn, Integer copies, MultipartFile imageFile) throws IOException, IOException;

    List<BookDetailsResponse> getAllBooks();

    BookDetailsResponse getBookById(Long id);
}
