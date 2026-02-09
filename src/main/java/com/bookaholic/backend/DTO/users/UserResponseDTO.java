package com.bookaholic.backend.DTO.users;

import com.bookaholic.backend.entity.enums.AuthProvider;
import com.bookaholic.backend.entity.enums.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private AuthProvider authProvider;
    private boolean isVerified;
    private long activeBorrows;
}
