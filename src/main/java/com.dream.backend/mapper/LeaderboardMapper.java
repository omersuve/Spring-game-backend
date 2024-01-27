package com.dream.backend.mapper;

import com.dream.backend.dto.CountryLeaderboardDto;
import com.dream.backend.dto.GroupLeaderboardDto;
import com.dream.backend.model.User;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardMapper {
    public static List<GroupLeaderboardDto> convertGroupLeaderboard(Iterable<ZSetOperations.TypedTuple<Long>> scores, List<User> users, String groupId) {
        List<GroupLeaderboardDto> groupLeaderboardData = new ArrayList<>();
        scores.forEach(score -> {
            GroupLeaderboardDto groupLeaderboardDto = new GroupLeaderboardDto();
            for (User user : users) {
                if (user.getId().equals(score.getValue())) {
                    groupLeaderboardDto.setId(score.getValue());
                    groupLeaderboardDto.setScore(score.getScore());
                    groupLeaderboardDto.setUsername(user.getUsername());
                    groupLeaderboardDto.setCountry(user.getCountry());
                    groupLeaderboardDto.setGroupId(groupId);
                    break;
                }
            }
            groupLeaderboardData.addLast(groupLeaderboardDto);
        });
        return groupLeaderboardData;
    }

    public static List<CountryLeaderboardDto> convertCountryLeaderboard(Iterable<ZSetOperations.TypedTuple<String>> scores) {
        List<CountryLeaderboardDto> updatedCountries = new ArrayList<>();
        scores.forEach(score -> {
            CountryLeaderboardDto countryLeaderboardDto = new CountryLeaderboardDto();
            countryLeaderboardDto.setCountry(score.getValue());
            countryLeaderboardDto.setScore(score.getScore());
            updatedCountries.addLast(countryLeaderboardDto);
        });
        return updatedCountries;
    }
}