package com.github.retro_game.retro_game.controller.form;

public class DeleteMessageForm {
  private long bodyId;
  private long messageId;

  public long getBodyId() {
    return bodyId;
  }

  public void setBodyId(long bodyId) {
    this.bodyId = bodyId;
  }

  public long getMessageId() {
    return messageId;
  }

  public void setMessageId(long messageId) {
    this.messageId = messageId;
  }
}
