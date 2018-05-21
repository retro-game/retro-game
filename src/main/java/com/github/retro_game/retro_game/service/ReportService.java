package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ReportService {
  CombatReportDto getCombatReport(long id, String token);

  EspionageReportDto getEspionageReport(long id, String token);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  ReportsSummaryDto getSummary(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<SimplifiedCombatReportDto> getSimplifiedCombatReports(long bodyId, SimplifiedCombatReportSortOrderDto sortOrder,
                                                             Sort.Direction direction, Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteSimplifiedCombatReport(long bodyId, long reportId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<SimplifiedEspionageReportDto> getSimplifiedEspionageReports(long bodyId, EspionageReportSortOrderDto sortOrder,
                                                                   Sort.Direction direction, Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteEspionageReport(long bodyId, long reportId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<HarvestReportDto> getHarvestReports(long bodyId, HarvestReportSortOrderDto sortOrder, Sort.Direction direction,
                                           Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteHarvestReport(long bodyId, long reportId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<TransportReportDto> getTransportReports(long bodyId, TransportReportSortOrderDto sortOrder,
                                               Sort.Direction direction, Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteTransportReport(long bodyId, long reportId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<OtherReportDto> getOtherReports(long bodyId, Pageable pageable);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void deleteOtherReport(long bodyId, long reportId);
}
