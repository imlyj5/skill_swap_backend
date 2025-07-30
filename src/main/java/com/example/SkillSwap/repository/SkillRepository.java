package com.example.SkillSwap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SkillSwap.model.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
}
