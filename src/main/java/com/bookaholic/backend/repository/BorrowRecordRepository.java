package com.bookaholic.backend.repository;

import com.bookaholic.backend.entity.BorrowRecord;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// BorrowRecord repository class

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // Find all records for a specific user (for User Dashboard) - Paginated
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    // Find records for a specific user filtered by status - Paginated
    Page<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowStatus status, Pageable pageable);

    // Find all records filtered by status (for Admin) - Paginated
    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);

    // Count how many books a user currently has borrowed (for Limit Check)
    long countByUserIdAndStatus(Long userId, BorrowStatus status);
}
