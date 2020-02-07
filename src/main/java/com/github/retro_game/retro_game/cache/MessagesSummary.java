package com.github.retro_game.retro_game.cache;

public class MessagesSummary {
  private final int numPrivateReceivedMessages;
  private final int numAllianceMessages;
  private final int numBroadcastMessages;

  public MessagesSummary(int numPrivateReceivedMessages, int numAllianceMessages, int numBroadcastMessages) {
    this.numPrivateReceivedMessages = numPrivateReceivedMessages;
    this.numAllianceMessages = numAllianceMessages;
    this.numBroadcastMessages = numBroadcastMessages;
  }

  public int getNumPrivateReceivedMessages() {
    return numPrivateReceivedMessages;
  }

  public int getNumAllianceMessages() {
    return numAllianceMessages;
  }

  public int getNumBroadcastMessages() {
    return numBroadcastMessages;
  }
}
