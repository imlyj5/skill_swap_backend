package com.example.SkillSwap.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.Users;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    
    // Find user by email for authentication
    Optional<Users> findByEmail(String email);
    
    // Check if email already exists (for signup validation)
    boolean existsByEmail(String email);
}
