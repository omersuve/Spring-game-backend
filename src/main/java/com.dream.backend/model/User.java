package com.dream.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Integer level = 1;
    @Enumerated(EnumType.STRING)
    private Country country;
    private Integer coin = 5000;
    private Integer rewardToClaim = -1;
    private boolean inTournament = false;
    private boolean waitingTournament = false;

    public User() {
    }
}
