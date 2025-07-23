package com.example.SkillSwap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Skill {

    private @Id @GeneratedValue Long id;
    private String name;
    private String category;

    public Skill() {}

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill skill)) return false;
        return Objects.equals(id, skill.id) &&
               Objects.equals(name, skill.name) &&
               Objects.equals(category, skill.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category);
    }

    @Override
    public String toString() {
        return "Skill{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", category='" + category + '\'' +
               '}';
    }
}
