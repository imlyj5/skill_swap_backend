package com.example.SkillSwap.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.UserOffers;

interface UserOffersRepository extends JpaRepository<UserOffers, Long> {

}
