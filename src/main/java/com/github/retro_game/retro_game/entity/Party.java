package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "parties")
@AllArgsConstructor
@NoArgsConstructor
public class Party {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private long id;

  @JoinColumn(name = "owner_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Getter
  private User owner;

  @JoinColumn(name = "target_user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Getter
  private User targetUser;

  @JoinColumn(name = "target_body_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Getter
  @Setter
  private Body targetBody;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "galaxy", column = @Column(name = "target_galaxy")),
      @AttributeOverride(name = "system", column = @Column(name = "target_system")),
      @AttributeOverride(name = "position", column = @Column(name = "target_position")),
      @AttributeOverride(name = "kind", column = @Column(name = "target_kind")),
  })
  @Getter
  @Setter
  private Coordinates targetCoordinates;

  @Column(name = "mission", nullable = false, updatable = false)
  @Getter
  private Mission mission;

  @JoinTable(
      name = "party_users",
      joinColumns = @JoinColumn(name = "party_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
  @ManyToMany
  @Getter
  @Setter
  private List<User> users;
}
