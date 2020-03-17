package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ReportService {
  CombatReportDto getCombatReport(long id, String token);

  EspionageReportDto getEspionageReport(long id, String token);

  @Activity(bodies = "#bodyId")
  ReportsSummaryDto getSummary(long bodyId);

  @Activity(bodies = "#bodyId")
  List<SimplifiedCombatReportDto> getSimplifiedCombatReports(long bodyId, SimplifiedCombatReportSortOrderDto sortOrder,
                                                             Sort.Direction direction, Pageable pageable);

  @Activity(bodies = "#bodyId")
  void deleteSimplifiedCombatReport(long bodyId, long reportId);

  @Activity(bodies = "#bodyId")
  void deleteAllSimplifiedCombatReports(long bodyId);

  @Activity(bodies = "#bodyId")
  List<SimplifiedEspionageReportDto> getSimplifiedEspionageReports(long bodyId, EspionageReportSortOrderDto sortOrder,
                                                                   Sort.Direction direction, Pageable pageable);

  @Activity(bodies = "#bodyId")
  void deleteEspionageReport(long bodyId, long reportId);

  @Activity(bodies = "#bodyId")
  void deleteAllEspionageReports(long bodyId);

  @Activity(bodies = "#bodyId")
  List<HarvestReportDto> getHarvestReports(long bodyId, HarvestReportSortOrderDto sortOrder, Sort.Direction direction,
                                           Pageable pageable);

  @Activity(bodies = "#bodyId")
  void deleteHarvestReport(long bodyId, long reportId);

  @Activity(bodies = "#bodyId")
  void deleteAllHarvestReports(long bodyId);

  @Activity(bodies = "#bodyId")
  List<TransportReportDto> getTransportReports(long bodyId, TransportReportSortOrderDto sortOrder,
                                               Sort.Direction direction, Pageable pageable);

  @Activity(bodies = "#bodyId")
  void deleteTransportReport(long bodyId, long reportId);

  @Activity(bodies = "#bodyId")
  void deleteAllTransportReports(long bodyId);

  @Activity(bodies = "#bodyId")
  List<OtherReportDto> getOtherReports(long bodyId, Pageable pageable);

  @Activity(bodies = "#bodyId")
  void deleteOtherReport(long bodyId, long reportId);

  @Activity(bodies = "#bodyId")
  void deleteAllOtherReports(long bodyId);
}
