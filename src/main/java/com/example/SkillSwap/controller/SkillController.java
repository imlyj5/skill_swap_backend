package com.example.SkillSwap.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.model.Skill;

//For future use of AI

@RestController
public class SkillController {
    private final SkillRepository skillRepository;
    
    SkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }
    
    @GetMapping("/skills")
    List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    
    @GetMapping("/skills/{id}")
    Skill getSkillById(@PathVariable Long id) {
        return skillRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
    }
    
    @PostMapping("/skills")
    Skill createSkill(@RequestBody Skill skill) {
        return skillRepository.save(skill);
    }
} 
