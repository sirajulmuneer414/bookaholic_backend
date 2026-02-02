package com.bookaholic.backend.service.book;

    // BorrowService interface for book borrowing operations

import com.bookaholic.backend.DTO.books.BorrowRecordResponseDTO;
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

    List<BorrowRecordResponseDTO> getAllRecords();
}
