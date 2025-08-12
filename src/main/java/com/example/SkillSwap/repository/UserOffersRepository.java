package com.example.SkillSwap.repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserOffers;

public interface UserOffersRepository extends JpaRepository<UserOffers, Long> {
    Optional<UserOffers> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    
    /**
     * Find all users who offer a specific skill
     * @param skillName the name of the skill to search for
     * @return List of UserOffers for users who offer this skill
     */
    List<UserOffers> findBySkillName(String skillName);
    
    /**
     * Find all UserOffers for a list of user IDs (bulk query)
     * @param userIds list of user IDs to search for
     * @return List of UserOffers for the specified users
     */
    List<UserOffers> findByUserIdIn(List<Long> userIds);
    

}
