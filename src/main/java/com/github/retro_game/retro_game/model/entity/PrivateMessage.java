package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "private_messages")
public class PrivateMessage {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "sender_id", updatable = false)
  private Long senderId;

  @Column(name = "recipient_id", updatable = false)
  private Long recipientId;

  @Column(name = "deleted_by_sender", nullable = false)
  private boolean deletedBySender;

  @Column(name = "deleted_by_recipient", nullable = false)
  private boolean deletedByRecipient;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "message", nullable = false, updatable = false)
  private String message;

  public long getId() {
    return id;
  }

  public Long getSenderId() {
    return senderId;
  }

  public void setSenderId(Long senderId) {
    this.senderId = senderId;
  }

  public Long getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(Long recipientId) {
    this.recipientId = recipientId;
  }

  public boolean isDeletedBySender() {
    return deletedBySender;
  }

  public void setDeletedBySender(boolean deletedBySender) {
    this.deletedBySender = deletedBySender;
  }

  public boolean isDeletedByRecipient() {
    return deletedByRecipient;
  }

  public void setDeletedByRecipient(boolean deletedByRecipient) {
    this.deletedByRecipient = deletedByRecipient;
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
