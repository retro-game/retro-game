package com.github.retro_game.retro_game.dto;

import java.util.Date;

public class AllianceApplicationDto {
  private final long id;
  private final long allianceId;
  private final String allianceTag;
  private final long userId;
  private final String userName;
  private final Date at;
  private final String applicationText;

  public AllianceApplicationDto(long id, long allianceId, String allianceTag, long userId, String userName, Date at,
                                String applicationText) {
    this.id = id;
    this.allianceId = allianceId;
    this.allianceTag = allianceTag;
    this.userId = userId;
    this.userName = userName;
    this.at = at;
    this.applicationText = applicationText;
  }

  public long getId() {
    return id;
  }

  public long getAllianceId() {
    return allianceId;
  }

  public String getAllianceTag() {
    return allianceTag;
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

  public String getApplicationText() {
    return applicationText;
  }
}
