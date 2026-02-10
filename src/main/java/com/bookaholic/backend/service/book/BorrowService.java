package com.bookaholic.backend.service.book;

// BorrowService interface for book borrowing operations

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
import com.bookaholic.backend.DTO.common.PagedResponse;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BorrowService {
    @Transactional
    BorrowRecordResponseDTO borrowBook(Long bookId);

    @Transactional
    BorrowRecordResponseDTO returnBook(Long recordId);

    // Admin Override Logic
    @Transactional
    BorrowRecordResponseDTO adminOverrideStatus(Long recordId, BorrowStatus status);

    List<BorrowRecordResponseDTO> getMyRecords();

    // Get my borrow records - Paginated
    PagedResponse<BorrowRecordResponseDTO> getMyRecords(int page, int size);

    // Get my borrow records filtered by status - Paginated
    PagedResponse<BorrowRecordResponseDTO> getMyRecords(int page, int size, BorrowStatus status);

    List<BorrowRecordResponseDTO> getAllRecords();

    // Get all borrow records - Paginated
    PagedResponse<BorrowRecordResponseDTO> getAllRecords(int page, int size);

    // Get all borrow records filtered by status - Paginated (for Admin)
    PagedResponse<BorrowRecordResponseDTO> getAllRecords(int page, int size, BorrowStatus status);

    // Get total count of all borrow records (for Admin header display)
    long getTotalRecordCount();
}
