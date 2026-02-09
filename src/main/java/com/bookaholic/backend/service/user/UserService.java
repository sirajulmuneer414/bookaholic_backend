package com.bookaholic.backend.service.user;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;

import java.util.List;

// UserService interface for admin user management operations

public interface UserService {

    // Get all users
    List<UserResponseDTO> getAllUsers();

    // Get user by ID
    UserResponseDTO getUserById(Long id);

    // Update user details
    UserResponseDTO updateUser(Long id, UserUpdateDTO dto);

    // Get user's borrow records
    List<BorrowRecordResponseDTO> getUserBorrowRecords(Long userId);

    // Update a borrow record (status, due date)
    BorrowRecordResponseDTO updateBorrowRecord(Long recordId, BorrowRecordUpdateDTO dto);
}
