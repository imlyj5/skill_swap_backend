package com.example.SkillSwap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class UserWants {
    private @Id
    @GeneratedValue Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Long skill_id;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getSkill_id() {
        return skill_id;
    }
    
    public void setSkill_id(Long skill_id) {
        this.skill_id = skill_id;
    }
}
