package com.example.SkillSwap.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserWants;

public interface UserWantsRepository extends JpaRepository<UserWants, Long> {
    List<UserWants> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
