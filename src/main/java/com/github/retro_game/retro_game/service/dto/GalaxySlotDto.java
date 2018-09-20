package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class GalaxySlotDto {
  private final long userId;
  private final String userName;
  private final boolean onVacation;
  private final boolean banned;
  private final String planetName;
  private final BodyTypeDto planetType;
  private final int planetImage;
  private final String moonName;
  private final Integer moonImage;
  private final int activity;
  private final Long debrisMetal;
  private final Long debrisCrystal;
  private final boolean own;

  public GalaxySlotDto(long userId, String userName, boolean onVacation, boolean banned, String planetName,
                       BodyTypeDto planetType, int planetImage, @Nullable String moonName, @Nullable Integer moonImage,
                       int activity, @Nullable Long debrisMetal, @Nullable Long debrisCrystal, boolean own) {
    this.userId = userId;
    this.userName = userName;
    this.onVacation = onVacation;
    this.banned = banned;
    this.planetName = planetName;
    this.planetType = planetType;
    this.planetImage = planetImage;
    this.moonName = moonName;
    this.moonImage = moonImage;
    this.activity = activity;
    this.debrisMetal = debrisMetal;
    this.debrisCrystal = debrisCrystal;
    this.own = own;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public boolean isOnVacation() {
    return onVacation;
  }

  public boolean isBanned() {
    return banned;
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

  public boolean isOwn() {
    return own;
  }
}
