package com.github.retro_game.retro_game.dto;

public class TechnologyDto {
  private final TechnologyKindDto kind;
  private final int currentLevel;
  private final int futureLevel;
  private final ResourcesDto cost;
  private final int requiredEnergy;
  private final long researchTime;
  private final int effectiveLabLevel;
  private final boolean canResearchNow;

  public TechnologyDto(TechnologyKindDto kind, int currentLevel, int futureLevel, ResourcesDto cost, int requiredEnergy,
                       long researchTime, int effectiveLabLevel, boolean canResearchNow) {
    this.kind = kind;
    this.currentLevel = currentLevel;
    this.futureLevel = futureLevel;
    this.cost = cost;
    this.requiredEnergy = requiredEnergy;
    this.researchTime = researchTime;
    this.effectiveLabLevel = effectiveLabLevel;
    this.canResearchNow = canResearchNow;
  }

  public TechnologyKindDto getKind() {
    return kind;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getFutureLevel() {
    return futureLevel;
  }

  public ResourcesDto getCost() {
    return cost;
  }

  public int getRequiredEnergy() {
    return requiredEnergy;
  }

  public long getResearchTime() {
    return researchTime;
  }

  public int getEffectiveLabLevel() {
    return effectiveLabLevel;
  }

  public boolean isCanResearchNow() {
    return canResearchNow;
  }
}
