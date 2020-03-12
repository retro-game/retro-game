package com.github.retro_game.retro_game.dto;

import java.io.Serializable;

public class MessagesSummaryDto implements Serializable {
  private final int numPrivateReceivedMessages;
  private final int numAllianceMessages;
  private final int numBroadcastMessages;

  public MessagesSummaryDto(int numPrivateReceivedMessages, int numAllianceMessages, int numBroadcastMessages) {
    this.numPrivateReceivedMessages = numPrivateReceivedMessages;
    this.numAllianceMessages = numAllianceMessages;
    this.numBroadcastMessages = numBroadcastMessages;
  }

  public int getTotalMessages() {
    return numPrivateReceivedMessages + numAllianceMessages + numBroadcastMessages;
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
