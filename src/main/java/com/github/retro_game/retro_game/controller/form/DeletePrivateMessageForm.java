package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.dto.PrivateMessageKindDto;

import javax.validation.constraints.NotNull;

public class DeletePrivateMessageForm {
  private long body;

  @NotNull
  private PrivateMessageKindDto kind;

  private long message;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public PrivateMessageKindDto getKind() {
    return kind;
  }

  public void setKind(PrivateMessageKindDto kind) {
    this.kind = kind;
  }

  public long getMessage() {
    return message;
  }

  public void setMessage(long message) {
    this.message = message;
  }
}
