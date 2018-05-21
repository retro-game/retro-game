package com.github.retro_game.retro_game.service.dto;

public class UserSettingsDto {
  private final String language;
  private final String skin;
  private final int numProbes;
  private final boolean numberInputScrollingEnabled;
  private final boolean showNewMessagesInOverviewEnabled;
  private final boolean showNewReportsInOverviewEnabled;

  public UserSettingsDto(String language, String skin, int numProbes, boolean numberInputScrollingEnabled,
                         boolean showNewMessagesInOverviewEnabled, boolean showNewReportsInOverviewEnabled) {
    this.language = language;
    this.skin = skin;
    this.numProbes = numProbes;
    this.numberInputScrollingEnabled = numberInputScrollingEnabled;
    this.showNewMessagesInOverviewEnabled = showNewMessagesInOverviewEnabled;
    this.showNewReportsInOverviewEnabled = showNewReportsInOverviewEnabled;
  }

  public String getLanguage() {
    return language;
  }

  public String getSkin() {
    return skin;
  }

  public int getNumProbes() {
    return numProbes;
  }

  public boolean isNumberInputScrollingEnabled() {
    return numberInputScrollingEnabled;
  }

  public boolean isShowNewMessagesInOverviewEnabled() {
    return showNewMessagesInOverviewEnabled;
  }

  public boolean isShowNewReportsInOverviewEnabled() {
    return showNewReportsInOverviewEnabled;
  }
}
