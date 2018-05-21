package com.github.retro_game.retro_game.service.dto;

import org.springframework.lang.Nullable;

import java.util.Date;

public class MessageDto {
  private final long id;
  private final Date at;
  private final Long senderId;
  private final String senderName;
  private final String message;

  public MessageDto(long id, Date at, @Nullable Long senderId, @Nullable String senderName, String message) {
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

  public Long getSenderId() {
    return senderId;
  }

  public String getSenderName() {
    return senderName;
  }

  public String getMessage() {
    return message;
  }
}
