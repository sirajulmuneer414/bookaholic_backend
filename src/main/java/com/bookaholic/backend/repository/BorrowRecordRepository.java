package com.bookaholic.backend.repository;

import com.bookaholic.backend.entity.BorrowRecord;
import com.bookaholic.backend.entity.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

    // BorrowRecord repository class

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // Find all records for a specific user (for User Dashboard)
    List<BorrowRecord> findByUserId(Long userId);

    // Count how many books a user currently has borrowed (for Limit Check)
    long countByUserIdAndStatus(Long userId, BorrowStatus status);
}
