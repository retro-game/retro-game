package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

public record UserContextDto(long id, String name, @Nullable Date vacationUntil, boolean forcedVacation,
                             UserSettingsDto settings, List<BodyInfoDto> bodies, BodyContextDto curBody,
                             BodiesPointersDto curBodyPointers) {
  public boolean isOnVacation() {
    return vacationUntil != null;
  }

  public boolean isBanned() {
    return forcedVacation && vacationUntil != null && vacationUntil.after(new Date());
  }
}
