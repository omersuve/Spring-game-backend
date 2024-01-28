package com.dream.backend.repository;

import com.dream.backend.model.Country;
import com.dream.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    User user1;
    User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setCountry(Country.UK);
        user1.setUsername("omer");
        user1.setInTournament(true);
        user1.setWaitingTournament(false);
        userRepository.save(user1);

        user2 = new User();
        user2.setCountry(Country.TURKEY);
        user2.setUsername("suve");
        user2.setInTournament(false);
        user2.setWaitingTournament(true);
        userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        user1 = null;
        user2 = null;
        userRepository.deleteAll();
    }


    @Test
    void testFindByInTournamentIsTrue() {
        List<User> users = userRepository.findByInTournamentIsTrue();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("omer");
    }

    @Test
    void testFindByWaitingTournamentIsTrue() {
        List<User> users = userRepository.findByWaitingTournamentIsTrue();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("suve");
    }

    @Test
    void testFindByIdIn() {
        List<Long> userIds = new ArrayList<>();
        userIds.addLast(user1.getId());
        userIds.addLast(user2.getId());
        List<User> users = userRepository.findByIdIn(userIds);
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.getFirst().getUsername()).isEqualTo("omer");
        assertThat(users.getLast().getUsername()).isEqualTo("suve");
    }
}
