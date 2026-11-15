package nogi.nozaki.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import nogi.nozaki.model.Match;
import nogi.nozaki.model.MatchMapper;

@Service
public class MatchService {

  private final MatchMapper matchMapper;

  public MatchService(MatchMapper matchMapper) {
    this.matchMapper = matchMapper;
  }

  public Match createMatch(String playerName) {
    Match match = new Match(UUID.randomUUID().toString(), playerName, "", "", "", "WAITING");
    matchMapper.insertMatch(match);
    return match;
  }

  public Match joinMatch(String matchId, String playerName) {
    Match match = findMatch(matchId);
    if (match == null) {
      throw new MatchNotFoundException();
    }
    if (!match.getBlack().isEmpty()) {
      throw new MatchFullException();
    }

    match.setBlack(playerName);
    matchMapper.updateBlack(match);
    return match;
  }

  public Match findMatch(String matchId) {
    return matchMapper.selectByMatchId(matchId);
  }

  public static class MatchNotFoundException extends RuntimeException {
  }

  public static class MatchFullException extends RuntimeException {
  }
}
