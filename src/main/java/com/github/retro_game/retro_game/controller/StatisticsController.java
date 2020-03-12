package com.github.retro_game.retro_game.controller;

import com.github.retro_game.retro_game.dto.PointsAndRankPairDto;
import com.github.retro_game.retro_game.dto.StatisticsDistributionDto;
import com.github.retro_game.retro_game.dto.StatisticsKindDto;
import com.github.retro_game.retro_game.dto.StatisticsPeriodDto;
import com.github.retro_game.retro_game.service.StatisticsService;
import io.vavr.Tuple2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.util.annotation.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Controller
@Validated
public class StatisticsController {
  private final StatisticsService statisticsService;

  public StatisticsController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @GetMapping("/statistics/summary")
  public String summary(@RequestParam(name = "body") long bodyId,
                        @RequestParam(name = "user") long userId,
                        Model model) {
    model.addAttribute("bodyId", bodyId);
    model.addAttribute("userId", userId);
    model.addAttribute("summary", statisticsService.getSummary(bodyId, userId));
    return "statistics-summary";
  }

  @GetMapping("/statistics/distinct-changes")
  public String distinctChanges(@RequestParam(name = "body") long bodyId,
                                @RequestParam(name = "user") long userId,
                                @RequestParam @Valid @NotNull StatisticsKindDto kind,
                                @RequestParam @Valid @NonNull StatisticsPeriodDto period,
                                Model model) {
    List<Tuple2<Date, PointsAndRankPairDto>> changes = statisticsService.getDistinctChanges(bodyId, userId, kind,
        period);
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Tuple2<Date, PointsAndRankPairDto> change : changes) {
      if (!first)
        builder.append(';');
      else
        first = false;
      builder.append(change._1.toInstant().getEpochSecond());
      builder.append(',');
      builder.append(change._2.getPoints());
      builder.append(',');
      builder.append(change._2.getRank());
    }

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("userId", userId);
    model.addAttribute("kind", kind);
    model.addAttribute("period", period);
    model.addAttribute("changes", builder.toString());

    return "statistics-distinct-changes";
  }

  @GetMapping("/statistics/distribution-changes")
  public String distributionChanges(@RequestParam(name = "body") long bodyId,
                                    @RequestParam(name = "user") long userId,
                                    @RequestParam @Valid @NonNull StatisticsPeriodDto period,
                                    Model model) {
    List<Tuple2<Date, StatisticsDistributionDto>> changes = statisticsService.getDistributionChanges(bodyId, userId,
        period);
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Tuple2<Date, StatisticsDistributionDto> change : changes) {
      if (!first)
        builder.append(';');
      else
        first = false;

      builder.append(change._1.toInstant().getEpochSecond());
      builder.append(',');

      StatisticsDistributionDto d = change._2;
      builder.append(d.getBuildings());
      builder.append(',');
      builder.append(d.getTechnologies());
      builder.append(',');
      builder.append(d.getFleet());
      builder.append(',');
      builder.append(d.getDefense());
    }

    model.addAttribute("bodyId", bodyId);
    model.addAttribute("userId", userId);
    model.addAttribute("period", period);
    model.addAttribute("changes", builder.toString());

    return "statistics-distribution-changes";
  }
}
