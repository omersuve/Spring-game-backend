package com.dream.backend.service;

import com.dream.backend.dto.CountryLeaderboardDto;
import com.dream.backend.dto.GroupLeaderboardDto;
import com.dream.backend.mapper.LeaderboardMapper;
import com.dream.backend.model.Country;
import com.dream.backend.model.User;
import com.dream.backend.repository.UserRepository;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {
    private final ZSetOperations<String, Long> groupLeaderboard;
    private final ZSetOperations<String, String> countryLeaderboard;
    private static boolean tournamentSet = false;
    private final UserRepository userRepository;
    private final TournamentQueueService tournamentQueueService;
    private final HashOperations<String, Long, String> userGroup;

    @Autowired
    public LeaderboardService(RedisTemplate<String, Long> redisTemplateGroup, RedisTemplate<String, String> redisTemplate, HashOperations<String, Long, String> userGroup, UserRepository userRepository, TournamentQueueService tournamentQueueService) {
        this.groupLeaderboard = redisTemplateGroup.opsForZSet();
        this.countryLeaderboard = redisTemplate.opsForZSet();
        this.userGroup = userGroup;
        this.userRepository = userRepository;
        this.tournamentQueueService = tournamentQueueService;
    }

    @Scheduled(cron = "0 42 15 * * *", zone = "UTC")
    private void setTournament() {
        tournamentSet = true;
        System.out.println("Flag set at 00:00 UTC");

        this.clearLeaderboardsAndUserGroupData();
        this.tournamentQueueService.clearTournamentGroups();

        Country[] countries = Country.values();

        for (Country c : countries) {
            this.addOrUpdateCountryScore(c, 0.0);
        }

    }

    @Scheduled(cron = "0 49 15 * * *", zone = "UTC")
    private void unsetTournament() {
        List<User> usersInTournament = userRepository.findByInTournamentIsTrue();
        List<User> usersWaitingTournament = userRepository.findByWaitingTournamentIsTrue();

        for (User user : usersWaitingTournament) {
            user.setWaitingTournament(false);
            userRepository.save(user);
        }
        for (User user : usersInTournament) {
            user.setInTournament(false);
            Long rank = getUserRankInGroup(user.getId());
            if (rank != null) {
                if (rank == 1L)
                    user.setRewardToClaim(10000);
                else if (rank == 2L)
                    user.setRewardToClaim(5000);
            }
            userRepository.save(user);
        }

        this.tournamentQueueService.clearTournamentQueue();
        System.out.println("Flag unset at 20:00 UTC");
        tournamentSet = false;
    }

    public static boolean isFlagSet() {
        return tournamentSet;
    }

    public void clearLeaderboardsAndUserGroupData() {
        Set<String> keys = this.groupLeaderboard.getOperations().keys("leaderboard:*");
        if (keys != null) {
            for (String key : keys)
                this.groupLeaderboard.removeRange(key, 0L, -1L);
        }

        this.userGroup.entries("UserGroup").keySet().forEach(hashKey ->
                this.userGroup.delete("UserGroup", hashKey));
    }

    public void addOrUpdateUserScore(Long userId, String groupId, double score) {
        if (tournamentSet) {
            String key = this.getGroupKey(groupId);
            this.groupLeaderboard.add(key, userId, score);
        }
    }

    public void addOrUpdateCountryScore(Country country, double score) {
        if (tournamentSet) {
            this.countryLeaderboard.add(this.getCountriesKey(), country.toString(), score);
        }
    }

    public void initUserToGroup(Long userId, String groupId) {
        if (tournamentSet) {
            this.userGroup.put("UserGroup", userId, groupId);
        }
    }

    public String getGroupOfUser(Long userId) {
        return this.userGroup.get("UserGroup", userId);
    }

    public Double getGroupScore(Long userId) {
        String groupId = this.getGroupOfUser(userId);
        return groupId != null ? this.groupLeaderboard.score(this.getGroupKey(groupId), userId) : null;
    }

    public Double getCountryScore(Country country) {
        return !tournamentSet ? null : this.countryLeaderboard.score(this.getCountriesKey(), country.toString());
    }

    public List<GroupLeaderboardDto> getGroupLeaderboard(String groupId) {
        String key = this.getGroupKey(groupId);
        Iterable<ZSetOperations.TypedTuple<Long>> groupLeaderboard =  this.groupLeaderboard.reverseRangeWithScores(key, 0L, -1L);
        if(groupLeaderboard == null) return null;
        List<Long> userIds = tournamentQueueService.getUsersInTournamentGroup(groupId);
        List<User> users = userRepository.findByIdIn(userIds);
        return LeaderboardMapper.convertGroupLeaderboard(groupLeaderboard, users, groupId);
    }

    public Long getUserRankInGroup(Long userId) {
        String groupId = this.getGroupOfUser(userId);
        String key = this.getGroupKey(groupId);
        Long rank = this.groupLeaderboard.reverseRank(key, userId);
        return rank != null ? rank + 1L : null;
    }

    public List<CountryLeaderboardDto> getCountryLeaderboard() {
        Iterable<ZSetOperations.TypedTuple<String>> countryLeaderboard = this.countryLeaderboard.reverseRangeWithScores(this.getCountriesKey(), 0L, -1L);
        if(countryLeaderboard == null) return null;
        return LeaderboardMapper.convertCountryLeaderboard(countryLeaderboard);
    }

    private String getGroupKey(String groupId) {
        return "leaderboard:" + groupId;
    }

    private String getCountriesKey() {
        return "country";
    }
}
