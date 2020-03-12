package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "alliances")
public class Alliance {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "owner_id")
  @OneToOne(fetch = FetchType.LAZY)
  private User owner;

  @Column(name = "tag", nullable = false)
  private String tag;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "is_recruitment_open", nullable = false)
  private boolean recruitmentOpen;

  @Column(name = "logo")
  private String logo;

  @Column(name = "external_text", nullable = false)
  private String externalText;

  @Column(name = "internal_text", nullable = false)
  private String internalText;

  @Column(name = "application_text", nullable = false)
  private String applicationText;

  @OneToMany(mappedBy = "key.alliance")
  private List<AllianceMember> members;

  @OneToMany(mappedBy = "alliance")
  private List<AllianceApplication> applications;

  public long getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isRecruitmentOpen() {
    return recruitmentOpen;
  }

  public void setRecruitmentOpen(boolean recruitmentOpen) {
    this.recruitmentOpen = recruitmentOpen;
  }

  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public String getExternalText() {
    return externalText;
  }

  public void setExternalText(String externalText) {
    this.externalText = externalText;
  }

  public String getInternalText() {
    return internalText;
  }

  public void setInternalText(String internalText) {
    this.internalText = internalText;
  }

  public String getApplicationText() {
    return applicationText;
  }

  public void setApplicationText(String applicationText) {
    this.applicationText = applicationText;
  }

  public List<AllianceMember> getMembers() {
    return members;
  }

  public List<AllianceApplication> getApplications() {
    return applications;
  }
}
