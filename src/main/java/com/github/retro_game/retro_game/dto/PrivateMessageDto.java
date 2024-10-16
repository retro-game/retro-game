package com.github.retro_game.retro_game.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class PrivateMessageDto {
  private final long id;
  private final Date at;

  @Nullable
  private final Long senderId;

  @Nullable
  private final String senderName;

  @Nullable
  private final Long recipientId;

  @Nullable
  private final String recipientName;

  private final String message;

  public PrivateMessageDto(long id, Date at, @Nullable Long senderId, @Nullable String senderName,
                           @Nullable Long recipientId, @Nullable String recipientName, String message) {
    this.id = id;
    this.at = at;
    this.senderId = senderId;
    this.senderName = senderName;
    this.recipientId = recipientId;
    this.recipientName = recipientName;
    this.message = message;
  }

  public long getId() {
    return id;
  }

  public Date getAt() {
    return at;
  }

  @Nullable
  public Long getSenderId() {
    return senderId;
  }

  @Nullable
  public String getSenderName() {
    return senderName;
  }

  @Nullable
  public Long getRecipientId() {
    return recipientId;
  }

  @Nullable
  public String getRecipientName() {
    return recipientName;
  }

  public String getMessage() {
    return message;
  }
}
