package com.example.SkillSwap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

//For AI-powered skill tagging

@Entity
@Table(name = "skill")
public class Skill {

    private @Id @GeneratedValue Long id;
    private String name;
    private String category;
    
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("tags")
    private List<String> tags;

    public Skill() {
        this.tags = new ArrayList<>();
    }

    public Skill(String name, String category) {
        this();
        this.name = name;
        this.category = category;
    }
    
    public Skill(String name, String category, List<String> tags) {
        this.name = name;
        this.category = category;
        this.tags = tags != null ? tags : new ArrayList<>();
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill skill)) return false;
        return Objects.equals(id, skill.id) &&
               Objects.equals(name, skill.name) &&
               Objects.equals(category, skill.category) &&
               Objects.equals(tags, skill.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, tags);
    }

    @Override
    public String toString() {
        return "Skill{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", category='" + category + '\'' +
               ", tags=" + tags +
               '}';
    }
}
