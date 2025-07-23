package com.example.SkillSwap.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
