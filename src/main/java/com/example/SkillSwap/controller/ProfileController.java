package com.example.SkillSwap.controller;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.model.User;


@RestController
public class ProfileController {
    private final UserRepository repository;
    ProfileController(UserRepository repository) {
        this.repository = repository;
      }

    @PatchMapping("/profiles/{id}")
    User editUser(@RequestBody User newUser, @PathVariable Long id) {
    
      return repository.findById(id)
        .map(user -> {
          user.setUsername(newUser.getUsername());
          user.setPronouns(newUser.getPronouns());
          user.setLocation(newUser.getLocation());
          user.setBio(newUser.getBio());
          user.setEmail(newUser.getEmail());
          user.setLearning_style(newUser.getLearning_style());
          user.setAvailability(newUser.getAvailability());
          user.setPassword(newUser.getPassword());
          user.setUserOffers(newUser.getUserOffers());
          return repository.save(user);
        })
        .orElseThrow(() -> new UserNotFoundException(id));
    }

    @DeleteMapping("/profiles/{id}")
    void deleteUser(@PathVariable Long id) {
      if (!repository.existsById(id)) {
        throw new UserNotFoundException(id);
      }
      repository.deleteById(id);
   }

    @GetMapping("/profiles")
    List<User> all() {
     return repository.findAll();
    }

    @GetMapping("/profiles/{id}")
    User getUser(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PostMapping("/profiles")
    User newUser(@RequestBody User newUser) {
      return repository.save(newUser);
    }

}
