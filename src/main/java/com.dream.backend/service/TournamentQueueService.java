package com.dream.backend.service;

import com.dream.backend.model.Country;
import com.dream.backend.model.TournamentGroup;
import com.dream.backend.model.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TournamentQueueService {
    private final ListOperations<String, String> tournamentQueue;
    private final HashOperations<String, String, TournamentGroup> tournamentGroups;

    @Autowired
    public TournamentQueueService(RedisTemplate<String, String> redisTemplate, HashOperations<String, String, TournamentGroup> hashOperationsForTournamentGroup) {
        this.tournamentQueue = redisTemplate.opsForList();
        this.tournamentGroups = hashOperationsForTournamentGroup;
    }

    // Clears the group queue cache
    public void clearTournamentQueue() {
        this.tournamentQueue.trim("GroupQueue", 0L, 0L);
    }

    // Clears the tournament group cache
    public void clearTournamentGroups() {
        this.tournamentGroups.entries("TournamentGroups").keySet().forEach(hashKey ->
                this.tournamentGroups.delete("TournamentGroups", hashKey));
    }

    // Process the queue and fill the group if 5 users from different countries are matched
    // Returns the TournamentGroup data if match occurs
    // Returns null if no match occurred yet
    public TournamentGroup processQueue(User user) {
        Long queueSize = this.tournamentQueue.size(this.getQueueName());
        if (queueSize == null) return null;
        if (queueSize == 0) {
            this.createTournamentGroupAddToQueue(user);
            return null;
        }
        for (int i = 0; i < queueSize; i++) {
            String groupId = this.tournamentQueue.index(this.getQueueName(), i);
            if (groupId == null) return null;
            if (this.tournamentGroups.hasKey(this.getHashName(), groupId)) {
                TournamentGroup existingGroup = this.tournamentGroups.get(this.getHashName(), groupId);
                if (existingGroup == null) return null;
                if (this.isGroupAppropriate(user.getCountry(), existingGroup)) {
                    TournamentGroup changed = this.addCountryAndUserToGroup(user.getCountry(), existingGroup, user.getId());
                    if (changed.getUsers().size() == 5) {
                        this.tournamentQueue.remove(this.getQueueName(), 0L, groupId);
                        this.tournamentGroups.put(this.getHashName(), groupId, changed);
                        return changed;
                    }
                    this.tournamentGroups.put(this.getHashName(), groupId, changed);
                    return null;
                }
            }
        }
        this.createTournamentGroupAddToQueue(user);
        return null;
    }

    // Creates a TournamentGroup and adds to the queue
    private void createTournamentGroupAddToQueue(User user) {
        TournamentGroup newObj = new TournamentGroup();
        TournamentGroup changed = this.addCountryAndUserToGroup(user.getCountry(), newObj, user.getId());
        this.addToQueue(changed.getId());
        this.tournamentGroups.put(this.getHashName(), changed.getId(), changed);
    }

    // Adds the groupId to the Queue
    private void addToQueue(String groupId) {
        this.tournamentQueue.rightPush(this.getQueueName(), groupId);
    }

    // Checks if given country can be added to the group or not
    private boolean isGroupAppropriate(Country country, TournamentGroup existingObj) {
        return switch (country) {
            case UK -> !existingObj.isUK();
            case TURKEY -> !existingObj.isTurkey();
            case USA -> !existingObj.isUSA();
            case FRANCE -> !existingObj.isFrance();
            case GERMANY -> !existingObj.isGerman();
        };
    }

    // Adds the given Country and UserId to the group
    private TournamentGroup addCountryAndUserToGroup(Country country, TournamentGroup existingObj, Long userId) {
        switch (country) {
            case UK:
                existingObj.setUK(true);
                break;
            case TURKEY:
                existingObj.setTurkey(true);
                break;
            case USA:
                existingObj.setUSA(true);
                break;
            case FRANCE:
                existingObj.setFrance(true);
                break;
            case GERMANY:
                existingObj.setGerman(true);
        }

        List<Long> currentUsers = existingObj.getUsers();
        currentUsers.addLast(userId);
        existingObj.setUsers(currentUsers);
        return existingObj;
    }

    // Gets the userIds within the given groupId
    public List<Long> getUsersInTournamentGroup(String groupId) {
        TournamentGroup existingGroup = this.tournamentGroups.get(this.getHashName(), groupId);
        if (existingGroup != null)
            return existingGroup.getUsers();
        return null;
    }

    // Gets hash name of TournamentGroups cache
    private String getHashName() {
        return "TournamentGroups";
    }

    // Gets queue name of GroupQueue Queue
    private String getQueueName() {
        return "GroupQueue";
    }
}
