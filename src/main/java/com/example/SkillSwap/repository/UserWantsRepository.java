package com.example.SkillSwap.repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserWants;

public interface UserWantsRepository extends JpaRepository<UserWants, Long> {
    Optional<UserWants> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    
    /**
     * Find all users who want a specific skill
     * @param skillName the name of the skill to search for
     * @return List of UserWants for users who want this skill
     */
    List<UserWants> findBySkillName(String skillName);
    
    /**
     * Find all UserWants for a list of user IDs (bulk query)
     * @param userIds list of user IDs to search for
     * @return List of UserWants for the specified users
     */
    List<UserWants> findByUserIdIn(List<Long> userIds);
}
