package com.bookaholic.backend.controller.book;

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.common.PagedResponse;
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

    // USER: See their history - Paginated
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-history")
    public ResponseEntity<PagedResponse<BorrowRecordResponseDTO>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BorrowStatus status) {
        log.info("Fetching user's borrowing history - page: {}, size: {}, status: {}", page, size, status);
        if (status != null) {
            return ResponseEntity.ok(service.getMyRecords(page, size, status));
        } else {
            return ResponseEntity.ok(service.getMyRecords(page, size));
        }
    }

    // USER: See their history - Unpaginated (for dashboard stats)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-history/all")
    public ResponseEntity<List<BorrowRecordResponseDTO>> getMyHistoryAll() {
        log.info("Fetching user's full borrowing history (unpaginated)");
        return ResponseEntity.ok(service.getMyRecords());
    }

    // ADMIN: See ALL history - Paginated
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<BorrowRecordResponseDTO>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BorrowStatus status) {
        log.info("Fetching all borrowing history - page: {}, size: {}, status: {}", page, size, status);
        if (status != null) {
            return ResponseEntity.ok(service.getAllRecords(page, size, status));
        } else {
            return ResponseEntity.ok(service.getAllRecords(page, size));
        }
    }

    // ADMIN: See ALL history - Unpaginated (for dashboard stats)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all/records")
    public ResponseEntity<List<BorrowRecordResponseDTO>> getAllHistoryUnpaginated() {
        log.info("Fetching all borrowing history (unpaginated)");
        return ResponseEntity.ok(service.getAllRecords());
    }

    // ADMIN: Override Status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/override/{recordId}")
    public ResponseEntity<BorrowRecordResponseDTO> overrideStatus(
            @PathVariable Long recordId,
            @RequestParam BorrowStatus status) {
        log.info("Admin overriding status for record id: {} to status: {}", recordId, status);
        return ResponseEntity.ok(service.adminOverrideStatus(recordId, status));
    }

    // ADMIN: Get total record count
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalRecordCount() {
        log.info("Fetching total borrow record count");
        return ResponseEntity.ok(service.getTotalRecordCount());
    }
}
