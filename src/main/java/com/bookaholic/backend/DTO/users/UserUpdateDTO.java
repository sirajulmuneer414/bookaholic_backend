package com.bookaholic.backend.DTO.users;

import com.bookaholic.backend.entity.enums.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {

    private String fullName;
    private Role role;
    private Boolean isVerified;
}
