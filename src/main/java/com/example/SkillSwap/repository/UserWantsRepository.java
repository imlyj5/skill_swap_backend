package com.example.SkillSwap.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserWants;

public interface UserWantsRepository extends JpaRepository<UserWants, Long> {
    Optional<UserWants> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
