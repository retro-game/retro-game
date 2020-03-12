package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.List;

public class OverviewBodiesDto {
  private final OverviewBodyInfoDto selectedBody;
  private final OverviewBodyBasicInfoDto associatedBody;
  private final List<OverviewBodyBasicInfoDto> otherPlanets;

  public OverviewBodiesDto(OverviewBodyInfoDto selectedBody, @Nullable OverviewBodyBasicInfoDto associatedBody,
                           List<OverviewBodyBasicInfoDto> otherPlanets) {
    this.selectedBody = selectedBody;
    this.associatedBody = associatedBody;
    this.otherPlanets = otherPlanets;
  }

  public OverviewBodyInfoDto getSelectedBody() {
    return selectedBody;
  }

  public OverviewBodyBasicInfoDto getAssociatedBody() {
    return associatedBody;
  }

  public List<OverviewBodyBasicInfoDto> getOtherPlanets() {
    return otherPlanets;
  }
}
