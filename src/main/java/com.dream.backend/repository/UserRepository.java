package com.dream.backend.repository;

import com.dream.backend.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByInTournamentIsTrue();

    List<User> findByWaitingTournamentIsTrue();

    List<User> findByIdIn(List<Long> userIds);
}
