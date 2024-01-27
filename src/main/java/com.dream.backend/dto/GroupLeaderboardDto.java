package com.dream.backend.dto;

import com.dream.backend.model.Country;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupLeaderboardDto {
    private Long id;
    private String username;
    private Country country;
    private Double score;
    private String groupId;
}