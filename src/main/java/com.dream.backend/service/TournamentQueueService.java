package com.dream.backend.service;

import com.dream.backend.model.Country;
import com.dream.backend.model.TournamentGroup;
import com.dream.backend.model.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
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

    public void clearTournamentQueue() {
        this.tournamentQueue.trim("GroupQueue", 0L, 0L);
    }

    public void clearTournamentGroups() {
        this.tournamentGroups.entries("TournamentGroups").keySet().forEach(hashKey ->
                this.tournamentGroups.delete("TournamentGroups", hashKey));
    }


    @Nullable
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
                    TournamentGroup changed = this.changeObject(user.getCountry(), existingGroup, user.getId());
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

    private void createTournamentGroupAddToQueue(User user) {
        TournamentGroup newObj = new TournamentGroup();
        TournamentGroup changed = this.changeObject(user.getCountry(), newObj, user.getId());
        this.addToQueue(this.getQueueName(), changed.getId());
        this.tournamentGroups.put(this.getHashName(), changed.getId(), changed);
    }

    private void addToQueue(String queueName, String groupId) {
        this.tournamentQueue.rightPush(queueName, groupId);
    }

    private boolean isGroupAppropriate(Country country, TournamentGroup existingObj) {
        return switch (country) {
            case UK -> !existingObj.isUK();
            case TURKEY -> !existingObj.isTurkey();
            case USA -> !existingObj.isUSA();
            case FRANCE -> !existingObj.isFrance();
            case GERMANY -> !existingObj.isGerman();
        };
    }

    private TournamentGroup changeObject(Country country, TournamentGroup existingObj, Long userId) {
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

    public List<Long> getUsersInTournamentGroup(String groupId) {
        TournamentGroup existingGroup = this.tournamentGroups.get(this.getHashName(), groupId);
        if (existingGroup != null)
            return existingGroup.getUsers();
        return null;
    }

    private String getHashName() {
        return "TournamentGroups";
    }

    private String getQueueName() {
        return "GroupQueue";
    }
}
