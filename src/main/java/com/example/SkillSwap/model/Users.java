package com.example.SkillSwap.model;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
public class Users {
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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-offers")
    private UserOffers userOffer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-wants")
    private UserWants userWant;

    // Default constructor required by JPA
    public Users() {
    }

    //custom constructor when we need to create a new instance but do not yet have an id
    public Users(String email, String password) {
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

    public UserOffers getUserOffer() {
        return userOffer;
    }

    public void setUserOffer(UserOffers userOffer) {
        this.userOffer = userOffer;
    }

    public UserWants getUserWant() {
        return userWant;
    }

    public void setUserWant(UserWants userWant) {
        this.userWant = userWant;
    }
}
