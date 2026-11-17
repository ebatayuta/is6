package nogi.nozaki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import nogi.nozaki.model.Match;
import nogi.nozaki.service.MatchService;

@Controller
public class MatchController {

  private final MatchService matchService;

  public MatchController(MatchService matchService) {
    this.matchService = matchService;
  }

  @GetMapping("/matches")
  public String matches() {
    return "matches";
  }

  @PostMapping("/matches/create")
  public String create(@RequestParam String playerName, RedirectAttributes redirectAttributes) {
    Match match = matchService.createMatch(playerName);
    return redirectToMatch(match.getMatchId(), playerName);
  }

  @PostMapping("/matches/join")
  public String join(@RequestParam String matchId, @RequestParam String playerName,
      RedirectAttributes redirectAttributes) {
    try {
      matchService.joinMatch(matchId, playerName);
      return redirectToMatch(matchId, playerName);
    } catch (MatchService.MatchNotFoundException exception) {
      redirectAttributes.addFlashAttribute("error", "対局が見つかりません。");
    } catch (MatchService.MatchFullException exception) {
      redirectAttributes.addFlashAttribute("error", "この対局は満員です。");
    }
    return "redirect:/matches";
  }

  @PostMapping("/matches/rejoin")
  public String rejoin(@RequestParam String matchId, @RequestParam String playerName,
      RedirectAttributes redirectAttributes) {
    try {
      matchService.rejoinMatch(matchId, playerName);
      return redirectToMatch(matchId, playerName);
    } catch (MatchService.MatchNotFoundException exception) {
      redirectAttributes.addFlashAttribute("error", "対局が見つかりません。");
    } catch (MatchService.MatchPlayerMismatchException exception) {
      redirectAttributes.addFlashAttribute("error", "この対局に参加したプレイヤー名と一致しません。");
    }
    return "redirect:/matches";
  }

  @GetMapping("/matches/{matchId}")
  public String match(@PathVariable String matchId, @RequestParam String playerName, Model model,
      RedirectAttributes redirectAttributes) {
    Match match = matchService.findMatch(matchId);
    if (match == null) {
      redirectAttributes.addFlashAttribute("error", "対局が見つかりません。");
      return "redirect:/matches";
    }

    model.addAttribute("match", match);
    model.addAttribute("playerName", playerName);
    model.addAttribute("color", playerName.equals(match.getWhite()) ? "白" : "黒");
    return "matches";
  }

  private String redirectToMatch(String matchId, String playerName) {
    String location = UriComponentsBuilder.fromPath("/matches/{matchId}")
        .queryParam("playerName", playerName)
        .buildAndExpand(matchId)
        .encode()
        .toUriString();
    return "redirect:" + location;
  }
}
