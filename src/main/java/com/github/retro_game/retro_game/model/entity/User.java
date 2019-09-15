package com.github.retro_game.retro_game.model.entity;

import org.springframework.data.domain.Sort;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Entity
@Table(name = "users")
public class User {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "roles", nullable = false)
  private int roles;

  @Column(name = "private_received_messages_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date privateReceivedMessagesSeenAt;

  @Column(name = "alliance_messages_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date allianceMessagesSeenAt;

  @Column(name = "broadcast_messages_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date broadcastMessagesSeenAt;

  @Column(name = "combat_reports_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date combatReportsSeenAt;

  @Column(name = "espionage_reports_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date espionageReportsSeenAt;

  @Column(name = "harvest_reports_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date harvestReportsSeenAt;

  @Column(name = "transport_reports_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date transportReportsSeenAt;

  @Column(name = "other_reports_seen_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date otherReportsSeenAt;

  @Column(name = "language", nullable = false)
  private String language;

  @Column(name = "skin", nullable = false)
  private String skin;

  @Column(name = "num_probes", nullable = false)
  private int numProbes;

  @Column(name = "bodies_sort_order", nullable = false)
  private BodiesSortOrder bodiesSortOrder;

  @Column(name = "bodies_sort_direction", nullable = false)
  private Sort.Direction bodiesSortDirection;

  @Column(name = "flags", nullable = false)
  private int flags;

  @Column(name = "vacation_until")
  @Temporal(TemporalType.TIMESTAMP)
  private Date vacationUntil;

  @Column(name = "forced_vacation", nullable = false)
  private boolean forcedVacation;

  @OneToMany(mappedBy = "user")
  @MapKey(name = "id")
  @OrderBy("id")
  private SortedMap<Long, Body> bodies;

  @OneToMany(mappedBy = "key.user")
  @MapKey(name = "key.kind")
  private Map<TechnologyKind, Technology> technologies;

  @OneToMany(mappedBy = "key.user")
  @MapKey(name = "key.sequence")
  @OrderBy("key.sequence")
  private SortedMap<Integer, TechnologyQueueEntry> technologyQueue;

  @JoinTable(
      name = "party_users",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "party_id", referencedColumnName = "id"))
  @ManyToMany
  private List<Party> parties;

  public boolean hasRole(int role) {
    assert (role & (role - 1)) == 0;
    return (roles & role) != 0;
  }

  public boolean hasFlag(int flag) {
    assert (flag & (flag - 1)) == 0;
    return (flags & flag) != 0;
  }

  public int getTechnologyLevel(TechnologyKind kind) {
    Technology tech = technologies.get(kind);
    return tech != null ? tech.getLevel() : 0;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getRoles() {
    return roles;
  }

  public void setRoles(int roles) {
    this.roles = roles;
  }

  public Date getPrivateReceivedMessagesSeenAt() {
    return privateReceivedMessagesSeenAt;
  }

  public void setPrivateReceivedMessagesSeenAt(Date privateReceivedMessagesSeenAt) {
    this.privateReceivedMessagesSeenAt = privateReceivedMessagesSeenAt;
  }

  public Date getAllianceMessagesSeenAt() {
    return allianceMessagesSeenAt;
  }

  public void setAllianceMessagesSeenAt(Date allianceMessagesSeenAt) {
    this.allianceMessagesSeenAt = allianceMessagesSeenAt;
  }

  public Date getBroadcastMessagesSeenAt() {
    return broadcastMessagesSeenAt;
  }

  public void setBroadcastMessagesSeenAt(Date broadcastMessagesSeenAt) {
    this.broadcastMessagesSeenAt = broadcastMessagesSeenAt;
  }

  public Date getCombatReportsSeenAt() {
    return combatReportsSeenAt;
  }

  public void setCombatReportsSeenAt(Date combatReportsSeenAt) {
    this.combatReportsSeenAt = combatReportsSeenAt;
  }

  public Date getEspionageReportsSeenAt() {
    return espionageReportsSeenAt;
  }

  public void setEspionageReportsSeenAt(Date espionageReportsSeenAt) {
    this.espionageReportsSeenAt = espionageReportsSeenAt;
  }

  public Date getHarvestReportsSeenAt() {
    return harvestReportsSeenAt;
  }

  public void setHarvestReportsSeenAt(Date harvestReportsSeenAt) {
    this.harvestReportsSeenAt = harvestReportsSeenAt;
  }

  public Date getTransportReportsSeenAt() {
    return transportReportsSeenAt;
  }

  public void setTransportReportsSeenAt(Date transportReportsSeenAt) {
    this.transportReportsSeenAt = transportReportsSeenAt;
  }

  public Date getOtherReportsSeenAt() {
    return otherReportsSeenAt;
  }

  public void setOtherReportsSeenAt(Date otherReportsSeenAt) {
    this.otherReportsSeenAt = otherReportsSeenAt;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getSkin() {
    return skin;
  }

  public void setSkin(String skin) {
    this.skin = skin;
  }

  public int getNumProbes() {
    return numProbes;
  }

  public void setNumProbes(int numProbes) {
    this.numProbes = numProbes;
  }

  public BodiesSortOrder getBodiesSortOrder() {
    return bodiesSortOrder;
  }

  public void setBodiesSortOrder(BodiesSortOrder bodiesSortOrder) {
    this.bodiesSortOrder = bodiesSortOrder;
  }

  public Sort.Direction getBodiesSortDirection() {
    return bodiesSortDirection;
  }

  public void setBodiesSortDirection(Sort.Direction bodiesSortDirection) {
    this.bodiesSortDirection = bodiesSortDirection;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public Date getVacationUntil() {
    return vacationUntil;
  }

  public void setVacationUntil(Date vacationUntil) {
    this.vacationUntil = vacationUntil;
  }

  public boolean isForcedVacation() {
    return forcedVacation;
  }

  public void setForcedVacation(boolean forcedVacation) {
    this.forcedVacation = forcedVacation;
  }

  public SortedMap<Long, Body> getBodies() {
    return bodies;
  }

  public Map<TechnologyKind, Technology> getTechnologies() {
    return technologies;
  }

  public SortedMap<Integer, TechnologyQueueEntry> getTechnologyQueue() {
    return technologyQueue;
  }

  public List<Party> getParties() {
    return parties;
  }
}
