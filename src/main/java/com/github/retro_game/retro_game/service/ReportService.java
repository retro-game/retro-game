package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ReportService {
  EspionageReportDto getEspionageReport(long id, String token);

  ReportsSummaryDto getSummary(long bodyId);

  List<SimplifiedCombatReportDto> getSimplifiedCombatReports(long bodyId, SimplifiedCombatReportSortOrderDto sortOrder,
                                                             Sort.Direction direction, Pageable pageable);

  void deleteSimplifiedCombatReport(long bodyId, long reportId);

  void deleteAllSimplifiedCombatReports(long bodyId);

  List<SimplifiedEspionageReportDto> getSimplifiedEspionageReports(long bodyId, EspionageReportSortOrderDto sortOrder,
                                                                   Sort.Direction direction, Pageable pageable);

  void deleteEspionageReport(long bodyId, long reportId);

  void deleteAllEspionageReports(long bodyId);

  List<HarvestReportDto> getHarvestReports(long bodyId, HarvestReportSortOrderDto sortOrder, Sort.Direction direction,
                                           Pageable pageable);

  void deleteHarvestReport(long bodyId, long reportId);

  void deleteAllHarvestReports(long bodyId);

  List<TransportReportDto> getTransportReports(long bodyId, TransportReportSortOrderDto sortOrder,
                                               Sort.Direction direction, Pageable pageable);

  void deleteTransportReport(long bodyId, long reportId);

  void deleteAllTransportReports(long bodyId);

  List<OtherReportDto> getOtherReports(long bodyId, Pageable pageable);

  void deleteOtherReport(long bodyId, long reportId);

  void deleteAllOtherReports(long bodyId);
}
