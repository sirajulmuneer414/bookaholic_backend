package com.bookaholic.backend.DTO.users;

import com.bookaholic.backend.entity.enums.BorrowStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRecordUpdateDTO {

    private BorrowStatus status;
    private LocalDate dueDate;
}
