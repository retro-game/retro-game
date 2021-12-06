package com.github.retro_game.retro_game.entity;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.LongArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
@TypeDef(name = "long-array", typeClass = LongArrayType.class)
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

  @Column(name = "technologies", nullable = false)
  @Type(type = "int-array")
  private int[] technologiesArray;

  @Column(name = "technology_queue", nullable = false)
  @Type(type = "long-array")
  private long[] technologyQueueArray;

  @OneToMany(mappedBy = "user")
  @MapKey(name = "id")
  @OrderBy("id")
  private SortedMap<Long, Body> bodies;

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

  public EnumMap<TechnologyKind, Integer> getTechnologies() {
    return SerializationUtils.deserializeItems(TechnologyKind.class, technologiesArray);
  }

  public void setTechnologies(Map<TechnologyKind, Integer> technologies) {
    technologiesArray = SerializationUtils.serializeItems(TechnologyKind.class, technologies);
  }

  public int getTechnologyLevel(TechnologyKind kind) {
    var index = kind.ordinal();
    var level = technologiesArray[index];
    assert level >= 0;
    return level;
  }

  public void setTechnologyLevel(TechnologyKind kind, int level) {
    assert level >= 0;
    var index = kind.ordinal();
    technologiesArray[index] = level;
  }

  public SortedMap<Integer, TechnologyQueueEntry> getTechnologyQueue() {
    assert technologyQueueArray.length % 3 == 0;
    var numEntries = technologyQueueArray.length / 3;
    var queue = new TreeMap<Integer, TechnologyQueueEntry>();
    for (var i = 0; i < numEntries; i++) {
      var sequence = (int) technologyQueueArray[3 * i];
      var k = (int) technologyQueueArray[3 * i + 1];
      var kind = TechnologyKind.values()[k];
      var bodyId = technologyQueueArray[3 * i + 2];
      queue.put(sequence, new TechnologyQueueEntry(kind, bodyId));
    }
    return queue;
  }

  public void setTechnologyQueue(SortedMap<Integer, TechnologyQueueEntry> queue) {
    var array = new long[queue.size() * 3];
    var i = 0;
    for (var entry : queue.entrySet()) {
      array[3 * i] = entry.getKey();
      array[3 * i + 1] = entry.getValue().kind().ordinal();
      array[3 * i + 2] = entry.getValue().bodyId();
      i++;
    }
    technologyQueueArray = array;
  }

  public SortedMap<Long, Body> getBodies() {
    return bodies;
  }

  public List<Party> getParties() {
    return parties;
  }
}
