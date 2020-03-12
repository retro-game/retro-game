package com.github.retro_game.retro_game.dto;

import org.springframework.data.domain.Sort;

public class UserSettingsDto {
  private final String language;
  private final String skin;
  private final int numProbes;
  private final BodiesSortOrderDto bodiesSortOrder;
  private final Sort.Direction bodiesSortDirection;
  private final boolean numberInputScrollingEnabled;
  private final boolean showNewMessagesInOverviewEnabled;
  private final boolean showNewReportsInOverviewEnabled;
  private final boolean stickyMoonsEnabled;

  public UserSettingsDto(String language, String skin, int numProbes, BodiesSortOrderDto bodiesSortOrder,
                         Sort.Direction bodiesSortDirection, boolean numberInputScrollingEnabled,
                         boolean showNewMessagesInOverviewEnabled, boolean showNewReportsInOverviewEnabled,
                         boolean stickyMoonsEnabled) {
    this.language = language;
    this.skin = skin;
    this.numProbes = numProbes;
    this.bodiesSortOrder = bodiesSortOrder;
    this.bodiesSortDirection = bodiesSortDirection;
    this.numberInputScrollingEnabled = numberInputScrollingEnabled;
    this.showNewMessagesInOverviewEnabled = showNewMessagesInOverviewEnabled;
    this.showNewReportsInOverviewEnabled = showNewReportsInOverviewEnabled;
    this.stickyMoonsEnabled = stickyMoonsEnabled;
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

  public BodiesSortOrderDto getBodiesSortOrder() {
    return bodiesSortOrder;
  }

  public Sort.Direction getBodiesSortDirection() {
    return bodiesSortDirection;
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

  public boolean isStickyMoonsEnabled() {
    return stickyMoonsEnabled;
  }
}
