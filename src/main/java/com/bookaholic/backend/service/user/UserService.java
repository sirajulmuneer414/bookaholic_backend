package com.bookaholic.backend.service.user;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.common.PagedResponse;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;
import com.bookaholic.backend.entity.enums.Role;

import java.util.List;

// UserService interface for admin user management operations

public interface UserService {

    // Get all users
    List<UserResponseDTO> getAllUsers();

    // Get all users - Paginated
    PagedResponse<UserResponseDTO> getAllUsers(int page, int size);

    // Get all users filtered by role - Paginated
    PagedResponse<UserResponseDTO> getAllUsers(int page, int size, Role role);

    // Get total count of all users (for Admin header display)
    long getTotalUserCount();

    // Get user by ID
    UserResponseDTO getUserById(Long id);

    // Update user details
    UserResponseDTO updateUser(Long id, UserUpdateDTO dto);

    // Get user's borrow records
    List<BorrowRecordResponseDTO> getUserBorrowRecords(Long userId);

    // Get user's borrow records - Paginated
    PagedResponse<BorrowRecordResponseDTO> getUserBorrowRecords(Long userId, int page, int size);

    // Update a borrow record (status, due date)
    BorrowRecordResponseDTO updateBorrowRecord(Long recordId, BorrowRecordUpdateDTO dto);
}
