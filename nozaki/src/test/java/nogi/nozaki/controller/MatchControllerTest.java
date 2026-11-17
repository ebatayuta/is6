package nogi.nozaki.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;

import nogi.nozaki.model.Match;
import nogi.nozaki.service.MatchService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MatchService matchService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void whiteAndBlackPlayersCanRejoinTheirMatch() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/matches/create").param("playerName", "white-player"))
        .andReturn();
    String matchId = matchIdFromRedirect(createResult.getResponse().getRedirectedUrl());
    mockMvc.perform(post("/matches/join").param("matchId", matchId).param("playerName", "black-player"));

    mockMvc.perform(post("/matches/rejoin").param("matchId", matchId).param("playerName", "white-player"))
        .andExpect(redirectedUrl("/matches/" + matchId + "?playerName=white-player"));
    mockMvc.perform(post("/matches/rejoin").param("matchId", matchId).param("playerName", "black-player"))
        .andExpect(redirectedUrl("/matches/" + matchId + "?playerName=black-player"));

    mockMvc.perform(get("/matches/" + matchId).param("playerName", "white-player"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("白")));
    mockMvc.perform(get("/matches/" + matchId).param("playerName", "black-player"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(">黒</span>")));
  }

  @Test
  void rejoinRejectsDifferentPlayerNameWithoutChangingParticipants() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/matches/create").param("playerName", "white-player"))
        .andReturn();
    String matchId = matchIdFromRedirect(createResult.getResponse().getRedirectedUrl());
    mockMvc.perform(post("/matches/join").param("matchId", matchId).param("playerName", "black-player"));

    MvcResult rejoinResult = mockMvc.perform(post("/matches/rejoin")
        .param("matchId", matchId).param("playerName", "unknown-player"))
        .andExpect(redirectedUrl("/matches"))
        .andReturn();
    MockHttpSession session = (MockHttpSession) rejoinResult.getRequest().getSession();
    mockMvc.perform(get("/matches").session(session))
        .andExpect(content().string(containsString("この対局に参加したプレイヤー名と一致しません。")));

    Match match = matchService.findMatch(matchId);
    org.junit.jupiter.api.Assertions.assertEquals("white-player", match.getWhite());
    org.junit.jupiter.api.Assertions.assertEquals("black-player", match.getBlack());
  }

  @Test
  void finishedMatchCanBeRejoinedWithSavedBoardTurnAndResult() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/matches/create").param("playerName", "white-player"))
        .andReturn();
    String matchId = matchIdFromRedirect(createResult.getResponse().getRedirectedUrl());
    mockMvc.perform(post("/matches/join").param("matchId", matchId).param("playerName", "black-player"));
    jdbcTemplate.update("UPDATE match_info SET board = ?, turn = ?, state = ? WHERE matchId = ?",
        "saved-board", "black", "CHECKMATE_WHITE_WIN", matchId);

    MvcResult rejoinResult = mockMvc.perform(post("/matches/rejoin")
        .param("matchId", matchId).param("playerName", "white-player"))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    mockMvc.perform(get(rejoinResult.getResponse().getRedirectedUrl()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("saved-board")))
        .andExpect(content().string(containsString("black")))
        .andExpect(content().string(containsString("CHECKMATE_WHITE_WIN")));
  }

  @Test
  void matchesPageShowsRejoinForm() throws Exception {
    mockMvc.perform(get("/matches"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("/matches/rejoin")))
        .andExpect(content().string(containsString("再参加")));
  }

  @Test
  void createMatchRedirectsToWhiteMatchPage() throws Exception {
        MvcResult result = mockMvc.perform(post("/matches/create").param("playerName", "白プレイヤー"))
        .andExpect(status().is3xxRedirection())
        .andReturn();

        String matchId = matchIdFromRedirect(result.getResponse().getRedirectedUrl());
        mockMvc.perform(get("/matches/" + matchId).param("playerName", "白プレイヤー"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("白プレイヤー")))
        .andExpect(content().string(containsString("白")));
  }

  @Test
  void secondPlayerJoinsAsBlack() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/matches/create").param("playerName", "white-player"))
        .andReturn();
    String matchId = matchIdFromRedirect(createResult.getResponse().getRedirectedUrl());

    MvcResult joinResult = mockMvc.perform(post("/matches/join")
        .param("matchId", matchId).param("playerName", "black-player"))
        .andExpect(status().is3xxRedirection())
        .andReturn();

    mockMvc.perform(get(joinResult.getResponse().getRedirectedUrl()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("black-player")))
        .andExpect(content().string(containsString("黒")));
  }

  @Test
  void fullMatchRejectsThirdPlayerWithoutChangingParticipants() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/matches/create").param("playerName", "white-player"))
        .andReturn();
    String matchId = matchIdFromRedirect(createResult.getResponse().getRedirectedUrl());
    mockMvc.perform(post("/matches/join").param("matchId", matchId).param("playerName", "black-player"))
        .andReturn();

    mockMvc.perform(post("/matches/join").param("matchId", matchId).param("playerName", "third-player"))
        .andExpect(redirectedUrl("/matches"));
    mockMvc.perform(get("/matches/" + matchId).param("playerName", "black-player"))
        .andExpect(content().string(containsString("black-player")))
        .andExpect(content().string(not(containsString("third-player"))));
  }

  @Test
  void unknownMatchShowsErrorOnMatchesPage() throws Exception {
        MvcResult joinResult = mockMvc.perform(post("/matches/join").param("matchId", "missing-match")
        .param("playerName", "player"))
                .andExpect(redirectedUrl("/matches"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) joinResult.getRequest().getSession();
        mockMvc.perform(get("/matches").session(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("対局が見つかりません。")));
  }

  private String matchIdFromRedirect(String redirectUrl) {
    return redirectUrl.substring("/matches/".length(), redirectUrl.indexOf('?'));
  }
}
