package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.MessagesSummaryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MessagesController {
  private final MessagesSummaryService messagesSummaryService;

  public MessagesController(MessagesSummaryService messagesSummaryService) {
    this.messagesSummaryService = messagesSummaryService;
  }

  @GetMapping("/messages")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String messages(@RequestParam(name = "body") long bodyId, Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("summary", messagesSummaryService.get(bodyId));
    return "messages";
  }
}
