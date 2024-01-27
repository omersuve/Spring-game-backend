package com.dream.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountryLeaderboardDto {
    private String country;
    private Double score;
}