package com.example.SkillSwap.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SkillSwap.model.Skill;

interface SkillRepository extends JpaRepository<Skill, Long> {

<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SkillSwap.model.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
=======
>>>>>>> 4ad042c1596958b2c000c2040f25ba7460ea94a4
}
