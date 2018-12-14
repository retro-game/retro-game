package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

public class AllianceDto {
  private final long id;

  @Nullable
  private final Long ownerId;

  @Nullable
  private final String ownerName;

  private final String tag;
  private final String name;
  private final boolean recruitmentOpen;

  @Nullable
  private final String logo;

  private final String externalText;
  private final String internalText;

  private final int numMembers;
  private final int numApplications;
  private final boolean applyLinkVisible;
  private final boolean internalTextVisible;
  private final boolean applicationsLinkVisible;
  private final boolean memberListLinkVisible;
  private final boolean circularMessageLinkVisible;
  private final boolean manageLinkVisible;

  public AllianceDto(long id, @Nullable Long ownerId, @Nullable String ownerName, String tag, String name,
                     boolean recruitmentOpen, @Nullable String logo, String externalText, String internalText,
                     int numMembers, int numApplications, boolean applyLinkVisible, boolean internalTextVisible,
                     boolean applicationsLinkVisible, boolean memberListLinkVisible, boolean circularMessageLinkVisible,
                     boolean manageLinkVisible) {
    this.id = id;
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.tag = tag;
    this.name = name;
    this.recruitmentOpen = recruitmentOpen;
    this.logo = logo;
    this.externalText = externalText;
    this.internalText = internalText;
    this.numMembers = numMembers;
    this.numApplications = numApplications;
    this.applyLinkVisible = applyLinkVisible;
    this.internalTextVisible = internalTextVisible;
    this.applicationsLinkVisible = applicationsLinkVisible;
    this.memberListLinkVisible = memberListLinkVisible;
    this.circularMessageLinkVisible = circularMessageLinkVisible;
    this.manageLinkVisible = manageLinkVisible;
  }

  public long getId() {
    return id;
  }

  @Nullable
  public Long getOwnerId() {
    return ownerId;
  }

  @Nullable
  public String getOwnerName() {
    return ownerName;
  }

  public String getTag() {
    return tag;
  }

  public String getName() {
    return name;
  }

  public boolean isRecruitmentOpen() {
    return recruitmentOpen;
  }

  @Nullable
  public String getLogo() {
    return logo;
  }

  public String getExternalText() {
    return externalText;
  }

  public String getInternalText() {
    return internalText;
  }

  public int getNumMembers() {
    return numMembers;
  }

  public int getNumApplications() {
    return numApplications;
  }

  public boolean isApplyLinkVisible() {
    return applyLinkVisible;
  }

  public boolean isInternalTextVisible() {
    return internalTextVisible;
  }

  public boolean isApplicationsLinkVisible() {
    return applicationsLinkVisible;
  }

  public boolean isMemberListLinkVisible() {
    return memberListLinkVisible;
  }

  public boolean isCircularMessageLinkVisible() {
    return circularMessageLinkVisible;
  }

  public boolean isManageLinkVisible() {
    return manageLinkVisible;
  }
}
