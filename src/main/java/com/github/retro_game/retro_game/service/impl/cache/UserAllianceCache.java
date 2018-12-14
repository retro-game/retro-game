package com.github.retro_game.retro_game.service.impl.cache;

import com.github.retro_game.retro_game.model.entity.AllianceMember;
import com.github.retro_game.retro_game.model.repository.AllianceMemberRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserAllianceCache {
  private final AllianceMemberRepository allianceMemberRepository;
  private final ConcurrentHashMap<Long, Long> userAlliance = new ConcurrentHashMap<>();

  public UserAllianceCache(AllianceMemberRepository allianceMemberRepository) {
    this.allianceMemberRepository = allianceMemberRepository;
  }

  @PostConstruct
  private void loadUserAlliances() {
    for (AllianceMember member : allianceMemberRepository.findAll()) {
      long userId = member.getUser().getId();
      long allianceId = member.getAlliance().getId();
      Assert.isTrue(!userAlliance.containsKey(userId), "Inconsistent database state");
      userAlliance.put(userId, allianceId);
    }
  }

  @Nullable
  public Long getUserAlliance(long userId) {
    return userAlliance.get(userId);
  }

  public void updateUserAlliance(long userId, long allianceId) {
    userAlliance.put(userId, allianceId);
  }

  public void removeUserAlliance(long userId) {
    userAlliance.remove(userId);
  }
}
