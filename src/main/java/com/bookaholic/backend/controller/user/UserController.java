package com.bookaholic.backend.controller.user;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;
import com.bookaholic.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// User controller class for admin user management

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Admin fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Admin fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Update user details
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {
        log.info("Admin updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    // Get user's borrow records
    @GetMapping("/{id}/records")
    public ResponseEntity<List<BorrowRecordResponseDTO>> getUserBorrowRecords(@PathVariable Long id) {
        log.info("Admin fetching borrow records for user id: {}", id);
        return ResponseEntity.ok(userService.getUserBorrowRecords(id));
    }

    // Update a borrow record
    @PutMapping("/records/{recordId}")
    public ResponseEntity<BorrowRecordResponseDTO> updateBorrowRecord(
            @PathVariable Long recordId,
            @RequestBody BorrowRecordUpdateDTO dto) {
        log.info("Admin updating borrow record with id: {}", recordId);
        return ResponseEntity.ok(userService.updateBorrowRecord(recordId, dto));
    }
}
