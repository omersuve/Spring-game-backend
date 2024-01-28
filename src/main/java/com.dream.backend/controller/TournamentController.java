package com.dream.backend.controller;

import com.dream.backend.dto.CountryLeaderboardDto;
import com.dream.backend.response.ResponseHandler;
import com.dream.backend.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dream.backend.dto.GroupLeaderboardDto;

import java.util.List;

@RestController
@RequestMapping("/tournament")
public class TournamentController {
    private final LeaderboardService leaderboardService;

    @Autowired
    public TournamentController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/groupLeaderboard/{groupId}")
    public ResponseEntity<Object> getLeaderboardTournamentGroup(@PathVariable("groupId") String groupId) {
        List<GroupLeaderboardDto> leaderboard = this.leaderboardService.getGroupLeaderboard(groupId);
        if (leaderboard == null)
            return ResponseHandler.responseBuilder("No group found!", HttpStatus.NOT_FOUND, null);
        return ResponseHandler.responseBuilder("Group Leaderboard successfully fetched!", HttpStatus.OK, leaderboard);
    }

    @GetMapping("/countryLeaderboard")
    public ResponseEntity<Object> getLeaderboardCountryGroup() {
        List<CountryLeaderboardDto> leaderboard = this.leaderboardService.getCountryLeaderboard();
        return ResponseHandler.responseBuilder("Country Leaderboard successfully fetched!", HttpStatus.OK, leaderboard);
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<Object> getUserRankTournamentGroup(@PathVariable("userId") Long userId) {
        Long rank = this.leaderboardService.getUserRankInGroup(userId);
        if (rank == null)
            return ResponseHandler.responseBuilder("Rank not found!", HttpStatus.NOT_FOUND, null);
        return ResponseHandler.responseBuilder("Rank successfully fetched!", HttpStatus.OK, rank);
    }
}
