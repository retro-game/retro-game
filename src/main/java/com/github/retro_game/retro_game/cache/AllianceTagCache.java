package com.github.retro_game.retro_game.cache;

import com.github.retro_game.retro_game.entity.Alliance;
import com.github.retro_game.retro_game.repository.AllianceRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AllianceTagCache {
  private final AllianceRepository allianceRepository;
  private final ConcurrentHashMap<Long, String> tags = new ConcurrentHashMap<>();

  public AllianceTagCache(AllianceRepository allianceRepository) {
    this.allianceRepository = allianceRepository;
  }

  @PostConstruct
  private void loadTags() {
    for (Alliance alliance : allianceRepository.findAll()) {
      tags.put(alliance.getId(), alliance.getTag());
    }
  }

  @Nullable
  public String getTag(long id) {
    return tags.get(id);
  }

  public void updateTag(long id, String tag) {
    tags.put(id, tag);
  }

  public void removeTag(long id) {
    tags.remove(id);
  }
}
