package com.bookaholic.backend.repository;

import com.bookaholic.backend.entity.User;
import com.bookaholic.backend.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository for User entity

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // Find users by role - Paginated (for Admin filtering)
    Page<User> findByRole(Role role, Pageable pageable);
}
