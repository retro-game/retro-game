package com.github.retro_game.retro_game.cache;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessagesSummaryCache {
  private final ConcurrentHashMap<Long, MessagesSummary> summaries = new ConcurrentHashMap<>();

  @Nullable
  public MessagesSummary get(long userId) {
    return summaries.get(userId);
  }

  public void update(long userId, MessagesSummary summary) {
    summaries.put(userId, summary);
  }

  public void remove(long userId) {
    summaries.remove(userId);
  }

  public void removeAll() {
    summaries.clear();
  }
}
