package com.dream.backend.service;

import com.dream.backend.model.Country;
import com.dream.backend.model.TournamentGroup;
import com.dream.backend.model.User;
import com.dream.backend.repository.UserRepository;
import com.dream.backend.response.ResponseHandler;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TournamentQueueService tournamentQueueService;
    private final LeaderboardService leaderboardService;

    @Autowired
    public UserService(UserRepository userRepository, TournamentQueueService tournamentQueueService, LeaderboardService leaderboardService) {
        this.userRepository = userRepository;
        this.tournamentQueueService = tournamentQueueService;
        this.leaderboardService = leaderboardService;
    }

    // Gets the User by id or returns null
    public User getUser(Long userId) {
        Optional<User> user = this.userRepository.findById(userId);
        return user.orElse(null);
    }

    // Creates a User, sets default values and a random Country
    public ResponseEntity<Object> createUser(User user) {
        try {
            user.setCountry(this.getRandomCountry());
            user.setLevel(1);
            user.setCoin(5000);
            user.setRewardToClaim(-1);
            user.setInTournament(false);
            user.setWaitingTournament(false);
            return ResponseHandler.responseBuilder("User created successfully!", HttpStatus.OK, this.userRepository.save(user));
        } catch (Exception e) {
            System.out.printf("Error: %s%n", e);
            return ResponseHandler.responseBuilder("Error while creating the User!", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    // Updates the User
    public void updateUser(User user) {
        try {
            this.userRepository.save(user);
        } catch (Exception e) {
            System.out.printf("Error: %s%n", e);
        }
    }

    // Increments the User level by 1
    // Increments the User gold by 25
    // Updates the leaderboards if given User has entered the tournament
    public ResponseEntity<Object> incrementUserLevel(Long userId) {
        try {
            User user = this.getUser(userId);
            if (user != null) {
                user.setLevel(user.getLevel() + 1);
                user.setCoin(user.getCoin() + 25);
                this.updateUser(user);
                String groupId = this.leaderboardService.getGroupOfUser(userId);
                if (groupId != null) {
                    Double currentScore = this.leaderboardService.getGroupScore(userId);
                    if (currentScore != null)
                        this.leaderboardService.addOrUpdateUserScore(userId, groupId, currentScore + 1.0);
                    Double currentCountryScore = this.leaderboardService.getCountryScore(user.getCountry());
                    if (currentCountryScore != null)
                        this.leaderboardService.addOrUpdateCountryScore(user.getCountry(), currentCountryScore + 1.0);
                    return ResponseHandler.responseBuilder("User level up successfully and its score incremented in tournament!", HttpStatus.OK, user);
                } else
                    return ResponseHandler.responseBuilder("User level up successfully!", HttpStatus.OK, user);
            } else
                return ResponseHandler.responseBuilder("User not found!", HttpStatus.NOT_FOUND, null);
        } catch (Exception e) {
            System.out.printf("Error: %s%n", e);
            return ResponseHandler.responseBuilder("Error while updating the User level!", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    // Provide User to claim the rewards of given user if there is any rewards to claim
    // Sets user reward to -1 after the claim
    public ResponseEntity<Object> claimReward(Long userId) {
        User user = this.getUser(userId);
        if (user == null) {
            return ResponseHandler.responseBuilder("User not found!", HttpStatus.NOT_FOUND, null);
        } else if (user.getRewardToClaim() == -1) {
            return ResponseHandler.responseBuilder("User has no reward to claim!", HttpStatus.NOT_FOUND, null);
        } else {
            user.setCoin(user.getCoin() + user.getRewardToClaim());
            user.setRewardToClaim(-1);
            this.updateUser(user);
            return ResponseHandler.responseBuilder(user.getRewardToClaim().toString() + " gold reward claimed successfully!", HttpStatus.OK, user);
        }
    }

    // Provide User to enter the tournament if there is a tournament to enter
    // Checks several conditions to be able to enter
    // After entering, it sets the user as waiting if there is not 5 people in the group
    // After entering, it sets the user as in tournament if 5 people matched
    public ResponseEntity<Object> enterTournament(Long userId) {
        if (!LeaderboardService.isFlagSet())
            return ResponseHandler.responseBuilder("There is no tournament!", HttpStatus.NOT_FOUND, null);
        else {
            User user = this.getUser(userId);
            if (user == null)
                return ResponseHandler.responseBuilder("User not found!", HttpStatus.NOT_FOUND, null);
            else if (user.isWaitingTournament())
                return ResponseHandler.responseBuilder("User has already entered a tournament group!", HttpStatus.BAD_REQUEST, null);
            else if (user.getRewardToClaim() != -1)
                return ResponseHandler.responseBuilder("User cannot join the tournament due to having unclaimed rewards!", HttpStatus.BAD_REQUEST, null);
            else if (user.getLevel() < 20)
                return ResponseHandler.responseBuilder("User cannot join the tournament due to being under 20 level!", HttpStatus.BAD_REQUEST, null);
            else if (user.getCoin() < 1000)
                return ResponseHandler.responseBuilder("User doesn't have 1000 gold to participate!", HttpStatus.BAD_REQUEST, null);
            else if (user.isInTournament())
                return ResponseHandler.responseBuilder("User is already in a tournament group!", HttpStatus.BAD_REQUEST, null);
            else {
                user.setCoin(user.getCoin() - 1000);
                TournamentGroup group = this.tournamentQueueService.processQueue(user);
                if (group == null) {
                    user.setWaitingTournament(true);
                    this.updateUser(user);
                    return ResponseHandler.responseBuilder("User entered group but there is no 5 people yet!", HttpStatus.OK, null);
                } else {
                    for (int i = 0; i < 5; i++) {
                        Long groupUserId = group.getUsers().get(i);
                        User u = this.getUser(groupUserId);
                        u.setInTournament(true);
                        u.setWaitingTournament(false);
                        this.updateUser(u);
                        this.leaderboardService.addOrUpdateUserScore(groupUserId, group.getId(), 0.0);
                        this.leaderboardService.initUserToGroup(groupUserId, group.getId());
                    }
                    return ResponseHandler.responseBuilder("User entered group and tournament started!", HttpStatus.OK,
                            this.leaderboardService.getGroupLeaderboard(group.getId()));
                }
            }
        }
    }

    // Helper function to get random Country
    private Country getRandomCountry() {
        Country[] countries = Country.values();
        Random random = new Random();
        int randomIndex = random.nextInt(countries.length);
        return countries[randomIndex];
    }
}
