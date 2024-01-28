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
    private final UserRepository userRepository;
    private final TournamentQueueService tournamentQueueService;
    private final HashOperations<String, Long, String> userGroup;
    private static boolean tournamentSet = false;

    @Autowired
    public LeaderboardService(RedisTemplate<String, Long> redisTemplateGroup, RedisTemplate<String, String> redisTemplate, HashOperations<String, Long, String> userGroup, UserRepository userRepository, TournamentQueueService tournamentQueueService) {
        this.groupLeaderboard = redisTemplateGroup.opsForZSet();
        this.countryLeaderboard = redisTemplate.opsForZSet();
        this.userGroup = userGroup;
        this.userRepository = userRepository;
        this.tournamentQueueService = tournamentQueueService;
    }

    // Tournaments start at 00:00 UTC daily
    // Just after beginning, it clears the Leaderboard and User Groups data
    // Just after beginning, it clears the Tournament Groups data
    // Just after beginning, it resets Country scores
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
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

    // Tournaments end at 20:00 UTC daily
    // Just before ending, it clears the Tournament Queue
    // Just before ending, it clears the User values of waiting for a tournament
    // Just before ending, it clears the User values of in tournament
    // Just before ending, it rewards Users in rank 1 and 2
    @Scheduled(cron = "0 0 20 * * *", zone = "UTC")
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

    // Returns the tournament status
    public static boolean isFlagSet() {
        return tournamentSet;
    }

    // Clears the group leaderboard cache and group cache of users
    public void clearLeaderboardsAndUserGroupData() {
        Set<String> keys = this.groupLeaderboard.getOperations().keys("leaderboard:*");
        if (keys != null) {
            for (String key : keys)
                this.groupLeaderboard.removeRange(key, 0L, -1L);
        }

        this.userGroup.entries("UserGroup").keySet().forEach(hashKey ->
                this.userGroup.delete("UserGroup", hashKey));
    }

    // Performs the User to have the updated score in GroupLeaderboard cache for its groupId
    public void addOrUpdateUserScore(Long userId, String groupId, double score) {
        if (tournamentSet) {
            String key = this.getGroupKey(groupId);
            this.groupLeaderboard.add(key, userId, score);
        }
    }

    // Performs the Country to have the updated score in CountryLeaderboard cache
    public void addOrUpdateCountryScore(Country country, double score) {
        if (tournamentSet) {
            this.countryLeaderboard.add(this.getCountriesKey(), country.toString(), score);
        }
    }

    // Performs the User to have the groupId as its value in UserGroup cache
    public void initUserToGroup(Long userId, String groupId) {
        if (tournamentSet) {
            this.userGroup.put("UserGroup", userId, groupId);
        }
    }

    // Returns the group id of the given User
    public String getGroupOfUser(Long userId) {
        return this.userGroup.get("UserGroup", userId);
    }

    // Returns the score of the User within the group
    public Double getGroupScore(Long userId) {
        String groupId = this.getGroupOfUser(userId);
        return groupId != null ? this.groupLeaderboard.score(this.getGroupKey(groupId), userId) : null;
    }

    // Returns the score of the Country
    public Double getCountryScore(Country country) {
        return !tournamentSet ? null : this.countryLeaderboard.score(this.getCountriesKey(), country.toString());
    }

    // Returns the list of users competing within the given group id ordered by decreasing their score
    public List<GroupLeaderboardDto> getGroupLeaderboard(String groupId) {
        String key = this.getGroupKey(groupId);
        Iterable<ZSetOperations.TypedTuple<Long>> groupLeaderboard = this.groupLeaderboard.reverseRangeWithScores(key, 0L, -1L);
        if (groupLeaderboard == null) return null;
        List<Long> userIds = tournamentQueueService.getUsersInTournamentGroup(groupId);
        List<User> users = userRepository.findByIdIn(userIds);
        return LeaderboardMapper.convertGroupLeaderboard(groupLeaderboard, users, groupId);
    }

    // Returns the rank of the user
    public Long getUserRankInGroup(Long userId) {
        String groupId = this.getGroupOfUser(userId);
        String key = this.getGroupKey(groupId);
        Long rank = this.groupLeaderboard.reverseRank(key, userId);
        return rank != null ? rank + 1L : null;
    }

    // Returns the list of Countries competing ordered by decreasing their score
    public List<CountryLeaderboardDto> getCountryLeaderboard() {
        Iterable<ZSetOperations.TypedTuple<String>> countryLeaderboard = this.countryLeaderboard.reverseRangeWithScores(this.getCountriesKey(), 0L, -1L);
        if (countryLeaderboard == null) return null;
        return LeaderboardMapper.convertCountryLeaderboard(countryLeaderboard);
    }

    // Returns the key for the given groupId of the GroupLeaderboard cache
    private String getGroupKey(String groupId) {
        return "leaderboard:" + groupId;
    }

    // Returns the key of the CountryLeaderboard cache
    private String getCountriesKey() {
        return "country";
    }
}
