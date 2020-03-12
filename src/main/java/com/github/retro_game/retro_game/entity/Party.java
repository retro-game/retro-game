package com.github.retro_game.retro_game.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "parties")
public class Party {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "owner_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @JoinColumn(name = "target_user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User targetUser;

  @JoinColumn(name = "target_body_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Body targetBody;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy", column = @Column(name = "target_galaxy")),
      @AttributeOverride(name = "system", column = @Column(name = "target_system")),
      @AttributeOverride(name = "position", column = @Column(name = "target_position")),
      @AttributeOverride(name = "kind", column = @Column(name = "target_kind")),
  })
  private Coordinates targetCoordinates;

  @Column(name = "mission", nullable = false, updatable = false)
  private Mission mission;

  @JoinTable(
      name = "party_users",
      joinColumns = @JoinColumn(name = "party_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
  @ManyToMany
  private List<User> users;

  public long getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public User getTargetUser() {
    return targetUser;
  }

  public void setTargetUser(User targetUser) {
    this.targetUser = targetUser;
  }

  public Body getTargetBody() {
    return targetBody;
  }

  public void setTargetBody(Body targetBody) {
    this.targetBody = targetBody;
  }

  public Coordinates getTargetCoordinates() {
    return targetCoordinates;
  }

  public void setTargetCoordinates(Coordinates targetCoordinates) {
    this.targetCoordinates = targetCoordinates;
  }

  public Mission getMission() {
    return mission;
  }

  public void setMission(Mission mission) {
    this.mission = mission;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }
}
