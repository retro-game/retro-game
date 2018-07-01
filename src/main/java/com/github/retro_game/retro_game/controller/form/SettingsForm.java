package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.service.dto.BodiesSortOrderDto;
import org.springframework.data.domain.Sort;
import reactor.util.annotation.NonNull;

import javax.validation.constraints.Min;

public class SettingsForm {
  private long body;

  @NonNull
  private BodiesSortOrderDto bodiesSortOrder;

  @NonNull
  private Sort.Direction bodiesSortDirection;

  private boolean stickyMoonsEnabled;

  @NonNull
  private String language;

  @NonNull
  private String skin;

  @Min(1)
  private int numProbes;

  private boolean numberInputScrollingEnabled;
  private boolean showNewMessagesInOverviewEnabled;
  private boolean showNewReportsInOverviewEnabled;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public BodiesSortOrderDto getBodiesSortOrder() {
    return bodiesSortOrder;
  }

  public void setBodiesSortOrder(BodiesSortOrderDto bodiesSortOrder) {
    this.bodiesSortOrder = bodiesSortOrder;
  }

  public Sort.Direction getBodiesSortDirection() {
    return bodiesSortDirection;
  }

  public void setBodiesSortDirection(Sort.Direction bodiesSortDirection) {
    this.bodiesSortDirection = bodiesSortDirection;
  }

  public boolean isStickyMoonsEnabled() {
    return stickyMoonsEnabled;
  }

  public void setStickyMoonsEnabled(boolean stickyMoonsEnabled) {
    this.stickyMoonsEnabled = stickyMoonsEnabled;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getSkin() {
    return skin;
  }

  public void setSkin(String skin) {
    this.skin = skin;
  }

  public int getNumProbes() {
    return numProbes;
  }

  public void setNumProbes(int numProbes) {
    this.numProbes = numProbes;
  }

  public boolean isNumberInputScrollingEnabled() {
    return numberInputScrollingEnabled;
  }

  public void setNumberInputScrollingEnabled(boolean numberInputScrollingEnabled) {
    this.numberInputScrollingEnabled = numberInputScrollingEnabled;
  }

  public boolean isShowNewMessagesInOverviewEnabled() {
    return showNewMessagesInOverviewEnabled;
  }

  public void setShowNewMessagesInOverviewEnabled(boolean showNewMessagesInOverviewEnabled) {
    this.showNewMessagesInOverviewEnabled = showNewMessagesInOverviewEnabled;
  }

  public boolean isShowNewReportsInOverviewEnabled() {
    return showNewReportsInOverviewEnabled;
  }

  public void setShowNewReportsInOverviewEnabled(boolean showNewReportsInOverviewEnabled) {
    this.showNewReportsInOverviewEnabled = showNewReportsInOverviewEnabled;
  }
}
