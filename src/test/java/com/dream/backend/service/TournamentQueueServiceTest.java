package com.dream.backend.service;

import com.dream.backend.model.Country;
import com.dream.backend.model.TournamentGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.MockitoAnnotations.openMocks;

public class TournamentQueueServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    private TournamentQueueService tournamentQueueService;
    private HashOperations<String, String, TournamentGroup> hashOperationsForTournamentGroup;
    AutoCloseable autoCloseable;
    TournamentGroup tournamentGroup;

    @BeforeEach
    void setUp() {
        this.autoCloseable = openMocks(this);
        this.tournamentQueueService = new TournamentQueueService(this.redisTemplate, this.hashOperationsForTournamentGroup);
        this.tournamentGroup = new TournamentGroup();
        this.tournamentGroup.setTurkey(true);
        this.tournamentGroup.setUsers(new ArrayList<>(Collections.singletonList(1L)));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    void isGroupAppropriateTrue() throws Exception {
        Method test = TournamentQueueService.class.getDeclaredMethod("isGroupAppropriate", Country.class, TournamentGroup.class);
        test.setAccessible(true);
        assertThat(test.invoke(this.tournamentQueueService, Country.USA, this.tournamentGroup)).isEqualTo(true);
    }

    @Test
    void isGroupAppropriateFalse() throws Exception {
        Method test = TournamentQueueService.class.getDeclaredMethod("isGroupAppropriate", Country.class, TournamentGroup.class);
        test.setAccessible(true);
        assertThat(test.invoke(this.tournamentQueueService, Country.TURKEY, this.tournamentGroup)).isEqualTo(false);
    }
}
