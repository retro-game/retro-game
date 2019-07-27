package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class GalaxySlotDto {
  private final long userId;
  private final String userName;
  private final int rank;
  private final boolean onVacation;
  private final boolean banned;
  private final NoobProtectionRankDto noobProtectionRank;
  private final String planetName;
  private final BodyTypeDto planetType;
  private final int planetImage;
  private final String moonName;
  private final Integer moonImage;
  private final int activity;
  private final Long debrisMetal;
  private final Long debrisCrystal;
  private final Long allianceId;
  private final String allianceTag;
  private final boolean own;
  private final boolean shortInactive;
  private final boolean longInactive;

  public GalaxySlotDto(long userId, String userName, int rank, boolean onVacation, boolean banned,
                       NoobProtectionRankDto noobProtectionRank, String planetName, BodyTypeDto planetType,
                       int planetImage, @Nullable String moonName, @Nullable Integer moonImage, int activity,
                       @Nullable Long debrisMetal, @Nullable Long debrisCrystal, @Nullable Long allianceId,
                       @Nullable String allianceTag, boolean own, boolean shortInactive, boolean longInactive) {
    this.userId = userId;
    this.userName = userName;
    this.rank = rank;
    this.onVacation = onVacation;
    this.banned = banned;
    this.noobProtectionRank = noobProtectionRank;
    this.planetName = planetName;
    this.planetType = planetType;
    this.planetImage = planetImage;
    this.moonName = moonName;
    this.moonImage = moonImage;
    this.activity = activity;
    this.debrisMetal = debrisMetal;
    this.debrisCrystal = debrisCrystal;
    this.allianceId = allianceId;
    this.allianceTag = allianceTag;
    this.own = own;
    this.shortInactive = shortInactive;
    this.longInactive = longInactive;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public int getRank() {
    return rank;
  }

  public boolean isOnVacation() {
    return onVacation;
  }

  public boolean isBanned() {
    return banned;
  }

  public NoobProtectionRankDto getNoobProtectionRank() {
    return noobProtectionRank;
  }

  public String getPlanetName() {
    return planetName;
  }

  public BodyTypeDto getPlanetType() {
    return planetType;
  }

  public int getPlanetImage() {
    return planetImage;
  }

  public String getMoonName() {
    return moonName;
  }

  public Integer getMoonImage() {
    return moonImage;
  }

  public int getActivity() {
    return activity;
  }

  public Long getDebrisMetal() {
    return debrisMetal;
  }

  public Long getDebrisCrystal() {
    return debrisCrystal;
  }

  public Long getAllianceId() {
    return allianceId;
  }

  public String getAllianceTag() {
    return allianceTag;
  }

  public boolean isOwn() {
    return own;
  }

  public boolean isShortInactive()
  {
    return shortInactive;
  }

  public boolean isLongInactive()
  {
    return longInactive;
  }
}
