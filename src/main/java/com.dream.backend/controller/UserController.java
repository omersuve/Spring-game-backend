package com.dream.backend.controller;

import com.dream.backend.model.User;
import com.dream.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Object> createUserDetails(@RequestBody User user) {
        return this.userService.createUser(user);
    }

    @PutMapping("/levelUp/{userId}")
    public ResponseEntity<Object> updateUserLevel(@PathVariable("userId") Long userId) {
        return this.userService.incrementUserLevel(userId);
    }

    @PostMapping("/enterTournament/{userId}")
    public ResponseEntity<Object> enterTournament(@PathVariable("userId") Long userId) {
        return this.userService.enterTournament(userId);
    }

    @PostMapping("/claimReward/{userId}")
    public ResponseEntity<Object> claimTournamentReward(@PathVariable("userId") Long userId) {
        return this.userService.claimReward(userId);
    }
}
