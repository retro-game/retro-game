package com.github.retro_game.retro_game.service.dto;

import java.util.List;

public class AllianceApplicationListDto {
  private final List<AllianceApplicationDto> applications;
  private final boolean processable;

  public AllianceApplicationListDto(List<AllianceApplicationDto> applications, boolean processable) {
    this.applications = applications;
    this.processable = processable;
  }

  public List<AllianceApplicationDto> getApplications() {
    return applications;
  }

  public boolean isProcessable() {
    return processable;
  }
}
