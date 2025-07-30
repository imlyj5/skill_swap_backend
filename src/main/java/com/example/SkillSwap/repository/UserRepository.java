package com.example.SkillSwap.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

}
