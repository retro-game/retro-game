package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class BroadcastMessageDto {
  private final long id;
  private final Date at;

  @Nullable
  private final Long senderId;

  @Nullable
  private final String senderName;

  private final String message;

  public BroadcastMessageDto(long id, Date at, @Nullable Long senderId, @Nullable String senderName, String message) {
    this.id = id;
    this.at = at;
    this.senderId = senderId;
    this.senderName = senderName;
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

  public String getMessage() {
    return message;
  }
}
