package com.github.retro_game.retro_game.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
public class Event {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "at", nullable = false)
  private Date at;

  @Column(name = "kind", nullable = false, updatable = false)
  private EventKind kind;

  @Column(name = "param")
  private Long param;

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public EventKind getKind() {
    return kind;
  }

  public void setKind(EventKind kind) {
    this.kind = kind;
  }

  public Long getParam() {
    return param;
  }

  public void setParam(Long param) {
    this.param = param;
  }
}
