package com.dream.backend.model;

import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@RedisHash("TournamentGroup")
public class TournamentGroup implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    private boolean isTurkey = false;
    private boolean isFrance = false;
    private boolean isGerman = false;
    private boolean isUK = false;
    private boolean isUSA = false;
    private List<Long> users = new ArrayList<>();

    public TournamentGroup() {
    }
}
