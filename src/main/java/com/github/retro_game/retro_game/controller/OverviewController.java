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
  private final MessagesSummaryService messagesSummaryService;
  private final ReportService reportService;
  private final StatisticsService statisticsService;
  private final UserService userService;

  public OverviewController(BodyService bodyService, FlightService flightService,
                            MessagesSummaryService messagesSummaryService, ReportService reportService,
                            StatisticsService statisticsService, UserService userService) {
    this.bodyService = bodyService;
    this.flightService = flightService;
    this.messagesSummaryService = messagesSummaryService;
    this.reportService = reportService;
    this.statisticsService = statisticsService;
    this.userService = userService;
  }

  @GetMapping("/overview")
  public String overview(@RequestParam(name = "body") long bodyId, Model model) {
    UserSettingsDto settings = userService.getCurrentUserSettings();
    model.addAttribute("numNewMessages", settings.isShowNewMessagesInOverviewEnabled() ?
        messagesSummaryService.get(bodyId).getTotalMessages() : 0);
    model.addAttribute("numNewReports", settings.isShowNewReportsInOverviewEnabled() ?
        reportService.getSummary(bodyId).getTotalReports() : 0);

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("serverTime", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("userName", userService.getCurrentUserName());
    model.addAttribute("flightEvents", flightService.getOverviewFlightEvents(bodyId));
    model.addAttribute("bodies", bodyService.getOverviewBodies(bodyId));
    model.addAttribute("summary", statisticsService.getCurrentUserSummary(bodyId));
    return "overview";
  }
}
