package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class EmpireDto {
  private final List<EmpireBodyDto> bodies;
  private final EmpireSummaryDto<Long> total;
  private final EmpireSummaryDto<Double> average;

  public EmpireDto(List<EmpireBodyDto> bodies, EmpireSummaryDto<Long> total, EmpireSummaryDto<Double> average) {
    this.bodies = bodies;
    this.total = total;
    this.average = average;
  }

  public List<EmpireBodyDto> getBodies() {
    return bodies;
  }

  public EmpireSummaryDto<Long> getTotal() {
    return total;
  }

  public EmpireSummaryDto<Double> getAverage() {
    return average;
  }
}
