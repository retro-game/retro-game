package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.model.entity.Ranking;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface RankingService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<? extends Ranking> findAll(long bodyId, String kind);
}
