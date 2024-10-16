package com.github.retro_game.retro_game.dto;

import java.util.List;

public class PartyDto {
  private final long id;
  private final long targetUserId;
  private final String targetUserName;
  private final String targetBodyName;
  private final CoordinatesDto targetCoordinates;
  private final List<PartyMemberDto> members;
  private final boolean canInvite;

  public PartyDto(long id, long targetUserId, String targetUserName, String targetBodyName,
                  CoordinatesDto targetCoordinates, List<PartyMemberDto> members, boolean canInvite) {
    this.id = id;
    this.targetUserId = targetUserId;
    this.targetUserName = targetUserName;
    this.targetBodyName = targetBodyName;
    this.targetCoordinates = targetCoordinates;
    this.members = members;
    this.canInvite = canInvite;
  }

  public long getId() {
    return id;
  }

  public long getTargetUserId() {
    return targetUserId;
  }

  public String getTargetUserName() {
    return targetUserName;
  }

  public String getTargetBodyName() {
    return targetBodyName;
  }

  public CoordinatesDto getTargetCoordinates() {
    return targetCoordinates;
  }

  public List<PartyMemberDto> getMembers() {
    return members;
  }

  public boolean isCanInvite() {
    return canInvite;
  }
}
