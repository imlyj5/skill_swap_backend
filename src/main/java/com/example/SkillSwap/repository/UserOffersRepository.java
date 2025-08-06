package com.example.SkillSwap.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserOffers;

public interface UserOffersRepository extends JpaRepository<UserOffers, Long> {
    Optional<UserOffers> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
