package com.example.SkillSwap.model;

import java.util.List;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    private @Id
    @GeneratedValue Long id;
    private String username;
    private String pronouns;
    private String location;
    private String bio;
    private String email;
    private String learning_style;
    private String availability;
    private String password;

    @OneToMany(mappedBy = "user")
    private List<UserOffers> userOffers;

    // Default constructor required by JPA
    public User() {
    }

    //custom constructor when we need to create a new instance but do not yet have an id
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLearning_style() {
        return learning_style;
    }

    public void setLearning_style(String learning_style) {
        this.learning_style = learning_style;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<UserOffers> getUserOffers() {
        return userOffers;
    }

    public void setUserOffers(List<UserOffers> userOffers) {
        this.userOffers = userOffers;
    }
}
