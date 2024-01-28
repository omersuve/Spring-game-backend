package com.dream.backend.controller;

import com.dream.backend.CaseApplication;
import com.dream.backend.dto.GroupLeaderboardDto;
import com.dream.backend.service.LeaderboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes= CaseApplication.class)
@WebMvcTest(TournamentController.class)
public class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LeaderboardService leaderboardService;
    GroupLeaderboardDto user1;
    GroupLeaderboardDto user2;
    List<GroupLeaderboardDto> leaderboard;

    @BeforeEach
    void setUp(){
        user1 = new GroupLeaderboardDto();
        user1.setGroupId("groupId1");
        user1.setUsername("omer");
        user2 = new GroupLeaderboardDto();
        user2.setGroupId("groupId1");
        user2.setUsername("suve");
        leaderboard = new ArrayList<>();
        leaderboard.add(user1);
        leaderboard.add(user2);
    }

    @AfterEach
    void tearDown(){
    }

    @Test
    void testGetLeaderboardTournamentGroup() throws Exception {
        when(leaderboardService.getGroupLeaderboard("groupId1")).thenReturn(leaderboard);
        this.mockMvc.perform(get("/tournament/groupLeaderboard/groupId1"))
                .andDo(print()).andExpect(status().isOk());
    }
}
