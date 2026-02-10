package com.bookaholic.backend.service.user.implementation;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.common.PagedResponse;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;
import com.bookaholic.backend.entity.Book;
import com.bookaholic.backend.entity.BorrowRecord;
import com.bookaholic.backend.entity.User;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import com.bookaholic.backend.entity.enums.Role;
import com.bookaholic.backend.repository.BookRepository;
import com.bookaholic.backend.repository.BorrowRecordRepository;
import com.bookaholic.backend.repository.UserRepository;
import com.bookaholic.backend.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * Get all users - Paginated
     *
     * @param page Page number (0-indexed)
     * @param size Number of records per page
     * @return PagedResponse with user details and pagination metadata
     */
    @Override
    public PagedResponse<UserResponseDTO> getAllUsers(int page, int size) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponseDTO> content = userPage.getContent().stream()
                .map(this::mapToUserResponseDTO)
                .toList();

        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious(),
                userPage.isFirst(),
                userPage.isLast());
    }

    /**
     * Get all users filtered by role - Paginated
     *
     * @param page Page number (0-indexed)
     * @param size Number of records per page
     * @param role Filter by user role (USER or ADMIN)
     * @return PagedResponse with filtered user details and pagination metadata
     */
    @Override
    public PagedResponse<UserResponseDTO> getAllUsers(int page, int size, Role role) {
        log.info("Fetching users by role: {} - page: {}, size: {}", role, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByRole(role, pageable);

        List<UserResponseDTO> content = userPage.getContent().stream()
                .map(this::mapToUserResponseDTO)
                .toList();

        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious(),
                userPage.isFirst(),
                userPage.isLast());
    }

    /**
     * Get total count of all users
     *
     * @return Total count of all users
     */
    @Override
    public long getTotalUserCount() {
        return userRepository.count();
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

        // For unpaginated version, we need to pass a Pageable.unpaged()
        return borrowRecordRepository.findByUserId(userId, Pageable.unpaged()).stream()
                .map(this::mapToBorrowRecordResponseDTO)
                .toList();
    }

    /**
     * Get user's borrow records - Paginated
     *
     * @param userId User ID
     * @param page   Page number (0-indexed)
     * @param size   Number of records per page
     * @return PagedResponse with borrow record details and pagination metadata
     */
    @Override
    public PagedResponse<BorrowRecordResponseDTO> getUserBorrowRecords(Long userId, int page, int size) {
        log.info("Fetching borrow records for user id: {} - page: {}, size: {}", userId, page, size);
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowRecord> recordPage = borrowRecordRepository.findByUserId(userId, pageable);

        List<BorrowRecordResponseDTO> content = recordPage.getContent().stream()
                .map(this::mapToBorrowRecordResponseDTO)
                .toList();

        return new PagedResponse<>(
                content,
                recordPage.getNumber(),
                recordPage.getSize(),
                recordPage.getTotalElements(),
                recordPage.getTotalPages(),
                recordPage.hasNext(),
                recordPage.hasPrevious(),
                recordPage.isFirst(),
                recordPage.isLast());
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
