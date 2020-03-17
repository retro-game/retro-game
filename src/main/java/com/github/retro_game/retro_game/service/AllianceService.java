package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.lang.Nullable;

import java.util.List;

public interface AllianceService {
  @Activity(bodies = "#bodyId")
  void create(long bodyId, String tag, String name);

  @Activity(bodies = "#bodyId")
  AllianceDto getById(long bodyId, long allianceId);

  @Activity(bodies = "#bodyId")
  @Nullable
  AllianceDto getCurrentUserAlliance(long bodyId);

  @Activity(bodies = "#bodyId")
  String getText(long bodyId, long allianceId, AllianceTextKindDto kind);

  @Activity(bodies = "#bodyId")
  List<AllianceMemberDto> getMembers(long bodyId, long allianceId);

  @Activity(bodies = "#bodyId")
  void leave(long bodyId, long allianceId);

  // Applications.

  @Activity(bodies = "#bodyId")
  void apply(long bodyId, long allianceId, String applicationText);

  @Activity(bodies = "#bodyId")
  @Nullable
  AllianceApplicationDto getCurrentUserApplication(long bodyId);

  @Activity(bodies = "#bodyId")
  void cancelCurrentUserApplication(long bodyId);

  @Activity(bodies = "#bodyId")
  AllianceApplicationListDto getApplications(long bodyId, long allianceId);

  @Activity(bodies = "#bodyId")
  void acceptApplication(long bodyId, long applicationId);

  @Activity(bodies = "#bodyId")
  void rejectApplication(long bodyId, long applicationId);

  // Management.

  @Activity(bodies = "#bodyId")
  void kickUser(long bodyId, long allianceId, long userIdToKick);

  @Activity(bodies = "#bodyId")
  void saveLogo(long bodyId, long allianceId, String url);

  @Activity(bodies = "#bodyId")
  void saveText(long bodyId, long allianceId, AllianceTextKindDto kind, String text);

  @Activity(bodies = "#bodyId")
  void disband(long bodyId, long allianceId, String password);
}
