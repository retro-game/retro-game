package com.github.retro_game.retro_game.controller.form;

import reactor.util.annotation.NonNull;

import javax.validation.constraints.Min;

public class SettingsForm {
  private long body;

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
