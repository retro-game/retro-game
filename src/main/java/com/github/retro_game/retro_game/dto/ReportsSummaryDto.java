package com.github.retro_game.retro_game.dto;

import java.io.Serializable;

public class ReportsSummaryDto implements Serializable {
  private final int numCombatReports;
  private final int numEspionageReports;
  private final int numHarvestReports;
  private final int numTransportReports;
  private final int numOtherReports;

  public ReportsSummaryDto(int numCombatReports, int numEspionageReports, int numHarvestReports,
                           int numTransportReports, int numOtherReports) {
    this.numCombatReports = numCombatReports;
    this.numEspionageReports = numEspionageReports;
    this.numHarvestReports = numHarvestReports;
    this.numTransportReports = numTransportReports;
    this.numOtherReports = numOtherReports;
  }

  public int getTotalReports() {
    return numCombatReports + numEspionageReports + numHarvestReports + numTransportReports + numOtherReports;
  }

  public int getNumCombatReports() {
    return numCombatReports;
  }

  public int getNumEspionageReports() {
    return numEspionageReports;
  }

  public int getNumHarvestReports() {
    return numHarvestReports;
  }

  public int getNumTransportReports() {
    return numTransportReports;
  }

  public int getNumOtherReports() {
    return numOtherReports;
  }
}
