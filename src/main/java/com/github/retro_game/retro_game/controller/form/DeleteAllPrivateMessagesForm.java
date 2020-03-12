package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.dto.PrivateMessageKindDto;

import javax.validation.constraints.NotNull;

public class DeleteAllPrivateMessagesForm {
  private long body;

  @NotNull
  private PrivateMessageKindDto kind;

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
}
