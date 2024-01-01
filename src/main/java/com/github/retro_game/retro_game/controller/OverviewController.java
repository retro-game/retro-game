package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.controller.activity.Activity;
import com.github.retro_game.retro_game.service.*;
import com.github.retro_game.retro_game.utils.Utils;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Date;

@Controller
public class OverviewController {
  private final BodyService bodyService;
  private final FlightEventsService flightEventsService;
  private final MessagesSummaryService messagesSummaryService;
  private final ReportService reportService;
  private final StatisticsService statisticsService;
  private final UserService userService;

  public OverviewController(BodyService bodyService, FlightEventsService flightEventsService,
                            MessagesSummaryService messagesSummaryService, ReportService reportService,
                            StatisticsService statisticsService, UserService userService) {
    this.bodyService = bodyService;
    this.flightEventsService = flightEventsService;
    this.messagesSummaryService = messagesSummaryService;
    this.reportService = reportService;
    this.statisticsService = statisticsService;
    this.userService = userService;
  }

  @GetMapping("/overview")
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS')")
  @Activity(bodies = "#bodyId")
  public String overview(@RequestParam(name = "body") long bodyId, Device device, Model model) {
    model.addAttribute("bodyId", bodyId);

    var userCtx = userService.getCurrentUserContext(bodyId);
    model.addAttribute("ctx", userCtx);

    var settings = userCtx.settings();
    model.addAttribute("numNewMessages", settings.isShowNewMessagesInOverviewEnabled() ? messagesSummaryService.get(bodyId).getTotalMessages() : 0);
    model.addAttribute("numNewReports", settings.isShowNewReportsInOverviewEnabled() ? reportService.getSummary(bodyId).getTotalReports() : 0);

    model.addAttribute("serverTime", Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())));
    model.addAttribute("flightEvents", flightEventsService.getOverviewFlightEvents(bodyId));
    model.addAttribute("bodies", bodyService.getOverviewBodies(bodyId));
    model.addAttribute("summary", statisticsService.getCurrentUserSummary(bodyId));

    return Utils.getAppropriateView(device, "overview");
  }
}
