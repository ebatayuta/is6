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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

  @Autowired
  private MockMvc mockMvc;

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
