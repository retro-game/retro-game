package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.*;
import org.springframework.lang.Nullable;

import java.util.List;

public interface AllianceService {
  void create(long bodyId, String tag, String name);

  AllianceDto getById(long bodyId, long allianceId);

  @Nullable
  AllianceDto getCurrentUserAlliance(long bodyId);

  String getText(long bodyId, long allianceId, AllianceTextKindDto kind);

  List<AllianceMemberDto> getMembers(long bodyId, long allianceId);

  void leave(long bodyId, long allianceId);

  // Applications.

  void apply(long bodyId, long allianceId, String applicationText);

  @Nullable
  AllianceApplicationDto getCurrentUserApplication(long bodyId);

  void cancelCurrentUserApplication(long bodyId);

  AllianceApplicationListDto getApplications(long bodyId, long allianceId);

  void acceptApplication(long bodyId, long applicationId);

  void rejectApplication(long bodyId, long applicationId);

  // Management.

  void kickUser(long bodyId, long allianceId, long userIdToKick);

  void saveLogo(long bodyId, long allianceId, String url);

  void saveText(long bodyId, long allianceId, AllianceTextKindDto kind, String text);

  void disband(long bodyId, long allianceId, String password);
}
