package com.bookaholic.backend.controller.user;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.common.PagedResponse;
import com.bookaholic.backend.DTO.users.BorrowRecordUpdateDTO;
import com.bookaholic.backend.DTO.users.UserResponseDTO;
import com.bookaholic.backend.DTO.users.UserUpdateDTO;
import com.bookaholic.backend.entity.enums.Role;
import com.bookaholic.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// User controller class for admin user management

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // Get all users
    /**
     * Get all users - Paginated
     * Default: page=0, size=10
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Role role) {
        log.info("Admin fetching all users - page: {}, size: {}, role: {}", page, size, role);
        if (role != null) {
            return ResponseEntity.ok(userService.getAllUsers(page, size, role));
        } else {
            return ResponseEntity.ok(userService.getAllUsers(page, size));
        }
    }

    // Get total user count
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalUserCount() {
        log.info("Admin fetching total user count");
        return ResponseEntity.ok(userService.getTotalUserCount());
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
    /**
     * Get specific user's borrow records - Paginated
     * Default: page=0, size=10
     */
    @GetMapping("/{id}/records")
    public ResponseEntity<PagedResponse<BorrowRecordResponseDTO>> getUserBorrowRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching borrow records for user id: {} - page: {}, size: {}", id, page, size);
        return ResponseEntity.ok(userService.getUserBorrowRecords(id, page, size));
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
