package com.bookaholic.backend.controller.book;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import com.bookaholic.backend.service.book.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    // Borrow controller class

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@Slf4j
public class BorrowController {

    private final BorrowService service;

    // USER: Borrow a book
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{bookId}")
    public ResponseEntity<BorrowRecordResponseDTO> borrowBook(@PathVariable Long bookId) {
        log.info("Borrowing book with id: {}", bookId);
        return ResponseEntity.ok(service.borrowBook(bookId));
    }

    // USER: Return their own book
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/return/{recordId}")
    public ResponseEntity<BorrowRecordResponseDTO> returnBook(@PathVariable Long recordId) {
        log.info("Returning book with record id: {}", recordId);
        return ResponseEntity.ok(service.returnBook(recordId));
    }

    // USER: See their history
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-history")
    public ResponseEntity<List<BorrowRecordResponseDTO>> getMyHistory() {
        log.info("Fetching user's borrowing history");
        return ResponseEntity.ok(service.getMyRecords());
    }

    // ADMIN: See ALL history
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<BorrowRecordResponseDTO>> getAllHistory() {
        log.info("Fetching all borrowing history");
        return ResponseEntity.ok(service.getAllRecords());
    }

    // ADMIN: Override Status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/override/{recordId}")
    public ResponseEntity<BorrowRecordResponseDTO> overrideStatus(
            @PathVariable Long recordId,
            @RequestParam BorrowStatus status
    ) {
        log.info("Admin overriding status for record id: {} to status: {}", recordId, status);
        return ResponseEntity.ok(service.adminOverrideStatus(recordId, status));
    }
}
