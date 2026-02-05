package com.bookaholic.backend.service.book.implementation;

import com.bookaholic.backend.DTO.books.BookDetailsResponse;
import com.bookaholic.backend.entity.Book;
import com.bookaholic.backend.repository.BookRepository;
import com.bookaholic.backend.service.book.BookService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Book service implementation

@Service
@RequiredArgsConstructor
public class BookServiceImp implements BookService {
        private final BookRepository repository;
        private final Cloudinary cloudinary;

        /**
         * Add Book to the library
         *
         * @param title     Title of the book
         * @param author    Author of the book
         * @param isbn      ISBN of the book
         * @param copies    Number of copies to add
         * @param imageFile Image file for the book cover
         *
         * @return BookDetailsResponse with added book details
         *
         * @throws IOException If there's an issue with image upload
         * @throws IOException If there's an issue with image upload
         */
        @Transactional
        @Override
        public BookDetailsResponse addBook(String title, String author, String isbn, Integer copies,
                        MultipartFile imageFile) throws IOException, IOException {
                // 1. Upload Image to Cloudinary
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                                "folder", "library_books"));
                String imageUrl = (String) uploadResult.get("url");

                // 2. Build and Save Book
                Book book = Book.builder()
                                .title(title)
                                .author(author)
                                .isbn(isbn)
                                .totalCopies(copies)
                                .availableCopies(copies) // Initially, all copies are available
                                .imageUrl(imageUrl)
                                .build();

                Book savedBook = repository.save(book);

                return BookDetailsResponse.builder()
                                .id(savedBook.getId())
                                .title(savedBook.getTitle())
                                .author(savedBook.getAuthor())
                                .isbn(savedBook.getIsbn())
                                .totalCopies(savedBook.getTotalCopies())
                                .availableCopies(savedBook.getAvailableCopies())
                                .imageUrl(savedBook.getImageUrl())
                                .build();
        }

        /**
         * Get all books from the library
         *
         * @return List of BookDetailsResponse with all books details
         */
        @Override
        public List<BookDetailsResponse> getAllBooks() {
                List<Book> bookList = repository.findAll();

                return bookList.stream().map(book -> BookDetailsResponse.builder()
                                .id(book.getId())
                                .title(book.getTitle())
                                .author(book.getAuthor())
                                .isbn(book.getIsbn())
                                .totalCopies(book.getTotalCopies())
                                .availableCopies(book.getAvailableCopies())
                                .imageUrl(book.getImageUrl())
                                .build()).toList();
        }

        /**
         * Get a book by its ID
         *
         * @param id ID of the book to retrieve
         *
         * @return BookDetailsResponse with book details
         *
         * @throws EntityNotFoundException If a book with given ID is not found
         */
        @Override
        public BookDetailsResponse getBookById(Long id) {
                return repository.findById(id)
                                .map(book -> BookDetailsResponse.builder()
                                                .id(book.getId())
                                                .title(book.getTitle())
                                                .author(book.getAuthor())
                                                .isbn(book.getIsbn())
                                                .totalCopies(book.getTotalCopies())
                                                .availableCopies(book.getAvailableCopies())
                                                .imageUrl(book.getImageUrl())
                                                .build())
                                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        }

        /**
         * Update an existing book in the library
         *
         * @param id          ID of the book to update
         * @param title       New title of the book
         * @param author      New author of the book
         * @param isbn        New ISBN of the book
         * @param totalCopies New total number of copies
         * @param imageFile   Optional new image file for the book cover (can be null to
         *                    keep existing)
         *
         * @return BookDetailsResponse with updated book details
         *
         * @throws EntityNotFoundException If book with given ID is not found
         * @throws IOException             If there's an issue with image upload
         */
        @Transactional
        @Override
        public BookDetailsResponse updateBook(Long id, String title, String author, String isbn, Integer totalCopies,
                        MultipartFile imageFile) throws IOException {
                // 1. Find existing book
                Book book = repository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));

                // 2. Calculate difference in copies
                int currentBorrowedCopies = book.getTotalCopies() - book.getAvailableCopies();

                // 3. Validate that totalCopies is not less than currently borrowed copies
                if (totalCopies < currentBorrowedCopies) {
                        throw new IllegalArgumentException(
                                        "Total copies (" + totalCopies
                                                        + ") cannot be less than currently borrowed copies ("
                                                        + currentBorrowedCopies + ")");
                }

                // 4. Upload new image if provided
                String imageUrl = book.getImageUrl(); // Keep existing by default
                if (imageFile != null && !imageFile.isEmpty()) {
                        Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                                        "folder", "library_books"));
                        imageUrl = (String) uploadResult.get("url");
                }

                // 5. Update book fields
                book.setTitle(title);
                book.setAuthor(author);
                book.setIsbn(isbn);
                book.setTotalCopies(totalCopies);
                book.setImageUrl(imageUrl);

                // 6. Recalculate available copies: new total - borrowed
                int newAvailableCopies = totalCopies - currentBorrowedCopies;
                book.setAvailableCopies(newAvailableCopies);

                // 7. Save updated book
                Book updatedBook = repository.save(book);

                return BookDetailsResponse.builder()
                                .id(updatedBook.getId())
                                .title(updatedBook.getTitle())
                                .author(updatedBook.getAuthor())
                                .isbn(updatedBook.getIsbn())
                                .totalCopies(updatedBook.getTotalCopies())
                                .availableCopies(updatedBook.getAvailableCopies())
                                .imageUrl(updatedBook.getImageUrl())
                                .build();
        }
}
