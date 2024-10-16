package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class AllianceMemberDto {
  private final long userId;
  private final String userName;
  private final Date joinedAt;

  @Nullable
  private final String rank;

  public AllianceMemberDto(long userId, String userName, Date joinedAt, @Nullable String rank) {
    this.userId = userId;
    this.userName = userName;
    this.joinedAt = joinedAt;
    this.rank = rank;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public Date getJoinedAt() {
    return joinedAt;
  }

  @Nullable
  public String getRank() {
    return rank;
  }
}
