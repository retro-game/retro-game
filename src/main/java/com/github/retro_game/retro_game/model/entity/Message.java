package com.github.retro_game.retro_game.model.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Message {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "recipient_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User recipient;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Column(name = "at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @JoinColumn(name = "sender_id", updatable = false)
  @ManyToOne(fetch = FetchType.EAGER)
  private User sender;

  @Column(name = "message", nullable = false, updatable = false)
  private String message;

  public long getId() {
    return id;
  }

  public User getRecipient() {
    return recipient;
  }

  public void setRecipient(User recipient) {
    this.recipient = recipient;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public User getSender() {
    return sender;
  }

  public void setSender(User sender) {
    this.sender = sender;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
