package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "alliance_messages")
public class AllianceMessage {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "alliance_id", nullable = false, updatable = false)
  private long allianceId;

  @Column(name = "sender_id", updatable = false)
  private Long senderId;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "message", nullable = false, updatable = false)
  private String message;

  public long getId() {
    return id;
  }

  public long getAllianceId() {
    return allianceId;
  }

  public void setAllianceId(long allianceId) {
    this.allianceId = allianceId;
  }

  public Long getSenderId() {
    return senderId;
  }

  public void setSenderId(Long senderId) {
    this.senderId = senderId;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
