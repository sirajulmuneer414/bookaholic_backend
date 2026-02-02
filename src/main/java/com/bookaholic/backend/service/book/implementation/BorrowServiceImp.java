package com.bookaholic.backend.service.book.implementation;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.entity.Book;
import com.bookaholic.backend.entity.BorrowRecord;
import com.bookaholic.backend.entity.User;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import com.bookaholic.backend.repository.BookRepository;
import com.bookaholic.backend.repository.BorrowRecordRepository;
import com.bookaholic.backend.repository.UserRepository;
import com.bookaholic.backend.service.book.BorrowService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

// BorrowService implementation class for book borrowing operations

@Service
@RequiredArgsConstructor
public class BorrowServiceImp implements BorrowService {

    private final BorrowRecordRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private static final int MAX_BORROW_LIMIT = 3;

    /**
     * Borrow Book Operation
     *
     * @param bookId Book ID to borrow
     *
     * @return BorrowRecordResponseDTO with borrow
     */
    @Transactional
    @Override
    public BorrowRecordResponseDTO borrowBook(Long bookId) {
        // 1. Get Current Logged-in User
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. Check Limit: User cannot borrow if they hit the max limit
        long activeBorrows = borrowRepository.countByUserIdAndStatus(user.getId(), BorrowStatus.BORROWED);
        if (activeBorrows >= MAX_BORROW_LIMIT) {
            throw new RuntimeException("User has reached the maximum borrowing limit of " + MAX_BORROW_LIMIT);
        }

        // 3. Check Book Availability
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is currently unavailable");
        }

        // 4. Update Book Inventory
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 5. Create Borrow Record
        BorrowRecord recordCreation = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2)) // 2 weeks due date
                .status(BorrowStatus.BORROWED)
                .build();

        BorrowRecord savedRecord = borrowRepository.save(recordCreation);

        return mapToResponseDTO(savedRecord);
    }

    /**
     * Return Book Operation
     *
     * @param recordId Record ID to return
     *
     * @return BorrowRecordResponseDTO with returned book
     */
    @Transactional
    @Override
    public BorrowRecordResponseDTO returnBook(Long recordId) {
        BorrowRecord recordToReturn = borrowRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (recordToReturn.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Book is already returned");
        }

        // 1. Update Record
        recordToReturn.setStatus(BorrowStatus.RETURNED);
        recordToReturn.setReturnDate(LocalDate.now());

        // 2. Update Book Inventory (Increase copies)
        Book book = recordToReturn.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        BorrowRecord returnedBookRecord =  borrowRepository.save(recordToReturn);

        return mapToResponseDTO(returnedBookRecord);
    }

    /**
     * Admin Override Status Operation
     *
     * @param recordId Record ID to override
     * @param status   New status to set
     *
     * @return BorrowRecordResponseDTO with overridden status
     */
    @Transactional
    @Override
    public BorrowRecordResponseDTO adminOverrideStatus(Long recordId, BorrowStatus status) {
        BorrowRecord recordToOverride = borrowRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        recordToOverride.setStatus(status);

        Book book = recordToOverride.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);



        BorrowRecord modRecord = borrowRepository.save(recordToOverride);

        return mapToResponseDTO(modRecord);
    }

    /**
     * Get My Records Operation
     *
     * @return List of BorrowRecordResponseDTO for current user
     */
    @Override
    public List<BorrowRecordResponseDTO> getMyRecords() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<BorrowRecord> recordList = borrowRepository.findByUserId(user.getId());

        return recordList.stream().map(this::mapToResponseDTO).toList();
    }

    /**
     * Get All Records Operation
     *
     * @return List of BorrowRecordResponseDTO for all users
     */
    @Override
    public List<BorrowRecordResponseDTO> getAllRecords() {
        return borrowRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }


    // --------------------------------------- HELPER METHODS -------------------------------------------------------------

    private BorrowRecordResponseDTO mapToResponseDTO(BorrowRecord recordToMap){
        return BorrowRecordResponseDTO.builder()
                .id(recordToMap.getId())
                .userId(recordToMap.getUser().getId())
                .userEmail(recordToMap.getUser().getEmail())
                .bookId(recordToMap.getBook().getId())
                .bookTitle(recordToMap.getBook().getTitle())
                .borrowDate(recordToMap.getBorrowDate())
                .dueDate(recordToMap.getDueDate())
                .status(recordToMap.getStatus())
                .build();
    }

}
