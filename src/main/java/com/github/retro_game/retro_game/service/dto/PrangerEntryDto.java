package com.github.retro_game.retro_game.service.dto;

import java.util.Date;

public class PrangerEntryDto {
  private final long userId;
  private final String userName;
  private final Date at;
  private final Date until;
  private final String reason;

  public PrangerEntryDto(long userId, String userName, Date at, Date until, String reason) {
    this.userId = userId;
    this.userName = userName;
    this.at = at;
    this.until = until;
    this.reason = reason;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public Date getAt() {
    return at;
  }

  public Date getUntil() {
    return until;
  }

  public String getReason() {
    return reason;
  }
}
