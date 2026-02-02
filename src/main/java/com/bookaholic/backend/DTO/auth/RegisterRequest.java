package com.bookaholic.backend.DTO.auth;

import com.bookaholic.backend.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    private Role role;

    /**
     * Required only when role == ADMIN
     */
    private String adminCode;
}
