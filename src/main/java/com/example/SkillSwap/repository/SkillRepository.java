package com.example.SkillSwap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SkillSwap.model.Skill;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    // Basic CRUD operations are inherited from JpaRepository
    
    /**
     * Find a skill by its name (returns first match if duplicates exist)
     * @param name the skill name to search for
     * @return Optional containing the skill if found, empty otherwise
     */
    Optional<Skill> findFirstByName(String name);
}
