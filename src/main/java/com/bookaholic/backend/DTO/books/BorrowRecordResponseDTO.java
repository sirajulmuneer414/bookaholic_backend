package com.bookaholic.backend.DTO.books;

import com.bookaholic.backend.entity.enums.BorrowStatus;
import lombok.*;

import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRecordResponseDTO {

    private Long id;

    private Long userId;

    private String userEmail;

    private Long bookId;

    private String bookTitle;

    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;

    private BorrowStatus status;
}
