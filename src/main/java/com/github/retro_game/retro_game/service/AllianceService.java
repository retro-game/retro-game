package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.service.dto.*;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AllianceService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void create(long bodyId, String tag, String name);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  AllianceDto getById(long bodyId, long allianceId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  @Nullable
  AllianceDto getCurrentUserAlliance(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  String getText(long bodyId, long allianceId, AllianceTextKindDto kind);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<AllianceMemberDto> getMembers(long bodyId, long allianceId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void leave(long bodyId, long allianceId);

  // Applications.

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void apply(long bodyId, long allianceId, String applicationText);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  @Nullable
  AllianceApplicationDto getCurrentUserApplication(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void cancelCurrentUserApplication(long bodyId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  AllianceApplicationListDto getApplications(long bodyId, long allianceId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void acceptApplication(long bodyId, long applicationId);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void rejectApplication(long bodyId, long applicationId);

  // Management.

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void kickUser(long bodyId, long allianceId, long userIdToKick);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void saveLogo(long bodyId, long allianceId, String url);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void saveText(long bodyId, long allianceId, AllianceTextKindDto kind, String text);

  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  void disband(long bodyId, long allianceId, String password);
}
