package com.bookaholic.backend.service.user.implementation;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;
import com.bookaholic.backend.entity.Book;
import com.bookaholic.backend.entity.BorrowRecord;
import com.bookaholic.backend.entity.User;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import com.bookaholic.backend.repository.BookRepository;
import com.bookaholic.backend.repository.BorrowRecordRepository;
import com.bookaholic.backend.repository.UserRepository;
import com.bookaholic.backend.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// UserService implementation class for admin user management operations

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;

    /**
     * Get all users
     *
     * @return List of UserResponseDTO
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return mapToUserResponseDTO(user);
    }

    /**
     * Update user details
     *
     * @param id  User ID
     * @param dto UserUpdateDTO with updated fields
     * @return Updated UserResponseDTO
     */
    @Transactional
    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Update only provided fields
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getIsVerified() != null) {
            user.setVerified(dto.getIsVerified());
        }

        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getEmail());
        return mapToUserResponseDTO(savedUser);
    }

    /**
     * Get user's borrow records
     *
     * @param userId User ID
     * @return List of BorrowRecordResponseDTO
     */
    @Override
    public List<BorrowRecordResponseDTO> getUserBorrowRecords(Long userId) {
        log.info("Fetching borrow records for user id: {}", userId);
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        return borrowRecordRepository.findByUserId(userId).stream()
                .map(this::mapToBorrowRecordResponseDTO)
                .toList();
    }

    /**
     * Update a borrow record (status, due date)
     *
     * @param recordId Record ID
     * @param dto      BorrowRecordUpdateDTO with updated fields
     * @return Updated BorrowRecordResponseDTO
     */
    @Transactional
    @Override
    public BorrowRecordResponseDTO updateBorrowRecord(Long recordId, BorrowRecordUpdateDTO dto) {
        log.info("Updating borrow record with id: {}", recordId);
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Borrow record not found with id: " + recordId));

        BorrowStatus oldStatus = record.getStatus();
        BorrowStatus newStatus = dto.getStatus();

        // Handle status change and book inventory
        if (newStatus != null && oldStatus != newStatus) {
            Book book = record.getBook();

            // BORROWED → RETURNED: Book is being returned, increase available copies
            if (oldStatus == BorrowStatus.BORROWED && newStatus == BorrowStatus.RETURNED) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                record.setReturnDate(LocalDate.now());
            }
            // RETURNED → BORROWED: Book is being re-borrowed, decrease available copies
            else if (oldStatus == BorrowStatus.RETURNED && newStatus == BorrowStatus.BORROWED) {
                if (book.getAvailableCopies() <= 0) {
                    throw new RuntimeException("Cannot change status to BORROWED: No available copies");
                }
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                record.setReturnDate(null);
            }

            bookRepository.save(book);
            record.setStatus(newStatus);
        }

        // Update due date if provided
        if (dto.getDueDate() != null) {
            record.setDueDate(dto.getDueDate());
        }

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        log.info("Borrow record updated successfully: {}", savedRecord.getId());
        return mapToBorrowRecordResponseDTO(savedRecord);
    }

    // --------------------------------------- HELPER METHODS
    // ------------------------------------------------------

    private UserResponseDTO mapToUserResponseDTO(User user) {
        long activeBorrows = borrowRecordRepository.countByUserIdAndStatus(user.getId(), BorrowStatus.BORROWED);

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .isVerified(user.isVerified())
                .activeBorrows(activeBorrows)
                .build();
    }

    private BorrowRecordResponseDTO mapToBorrowRecordResponseDTO(BorrowRecord record) {
        return BorrowRecordResponseDTO.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .userEmail(record.getUser().getEmail())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .borrowDate(record.getBorrowDate())
                .returnDate(record.getReturnDate())
                .dueDate(record.getDueDate())
                .status(record.getStatus())
                .build();
    }
}
