package com.example.SkillSwap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "user_wants")
public class UserWants {
    private @Id
    @GeneratedValue Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-wants")
    private Users user;
    
    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;
    
    private String skillName;  // Keep for backward compatibility
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Users getUser() {
        return user;
    }
    
    public void setUser(Users user) {
        this.user = user;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public Skill getSkill() {
        return skill;
    }
    
    public void setSkill(Skill skill) {
        this.skill = skill;
        // Update skillName for backward compatibility
        if (skill != null) {
            this.skillName = skill.getName();
        }
    }
}
