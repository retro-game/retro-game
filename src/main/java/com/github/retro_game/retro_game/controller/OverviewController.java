package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.service.*;
import com.github.retro_game.retro_game.service.dto.UserSettingsDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
public class OverviewController {
  private final BodyService bodyService;
  private final FlightService flightService;
  private final MessageService messageService;
  private final ReportService reportService;
  private final UserService userService;

  public OverviewController(BodyService bodyService, FlightService flightService, MessageService messageService,
                            ReportService reportService, UserService userService) {
    this.bodyService = bodyService;
    this.flightService = flightService;
    this.messageService = messageService;
    this.reportService = reportService;
    this.userService = userService;
  }

  @GetMapping("/overview")
  public String overview(@RequestParam(name = "body") long bodyId, Model model) {
    UserSettingsDto settings = userService.getCurrentUserSettings();
    model.addAttribute("numNewMessages", settings.isShowNewMessagesInOverviewEnabled() ?
        messageService.getNumNewMessages(bodyId) : 0);
    model.addAttribute("numNewReports", settings.isShowNewReportsInOverviewEnabled() ?
        reportService.getSummary(bodyId).getTotalReports() : 0);

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("serverTime", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("userName", userService.getCurrentUserName());
    model.addAttribute("flightEvents", flightService.getOverviewFlightEvents(bodyId));
    model.addAttribute("bodies", bodyService.getOverviewBodies(bodyId));
    return "overview";
  }
}
