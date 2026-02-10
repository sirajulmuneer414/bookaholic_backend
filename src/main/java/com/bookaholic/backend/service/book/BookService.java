package com.bookaholic.backend.service.book;

import com.bookaholic.backend.DTO.books.BookDetailsResponse;
import com.bookaholic.backend.DTO.common.PagedResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// Book service interface

public interface BookService {
        @Transactional
        BookDetailsResponse addBook(String title, String author, String isbn, Integer copies, MultipartFile imageFile)
                        throws IOException, IOException;

        @Transactional
        BookDetailsResponse updateBook(Long id, String title, String author, String isbn, Integer totalCopies,
                        MultipartFile imageFile) throws IOException;

        List<BookDetailsResponse> getAllBooks();

        // Paginated version
        PagedResponse<BookDetailsResponse> getAllBooks(int page, int size);

        BookDetailsResponse getBookById(Long id);
}
