package com.dream.backend.service;

import com.dream.backend.model.Country;
import com.dream.backend.model.User;
import com.dream.backend.repository.UserRepository;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private TournamentQueueService tournamentQueueService;
    private LeaderboardService leaderboardService;
    private  Object updateCountryScoreLock;
    AutoCloseable autoCloseable;
    User user;

    @BeforeEach
    void setUp() {
        this.autoCloseable = openMocks(this);
        this.userService = new UserService(this.userRepository, this.tournamentQueueService, this.leaderboardService, this.updateCountryScoreLock);
        this.user = new User();
        this.user.setUsername("omer");
        this.user.setCountry(Country.TURKEY);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    void testGetUser() {
        mock(User.class);
        mock(UserRepository.class);
        when(this.userRepository.findById(this.user.getId())).thenReturn(Optional.ofNullable(this.user));
        assertThat(this.userService.getUser(this.user.getId()).getCoin()).isEqualTo(this.user.getCoin());
    }

    @Test
    void testCreateUser() {
        mock(User.class);
        mock(UserRepository.class);
        when(this.userRepository.save(this.user)).thenReturn(this.user);
        assertThat(this.userService.createUser(this.user).getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetRandomCountry() throws Exception {
        Method test = UserService.class.getDeclaredMethod("getRandomCountry");
        test.setAccessible(true);
        assertThat(test.invoke(this.userService) instanceof Country).isTrue();
    }
}
