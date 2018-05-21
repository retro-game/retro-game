package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.Ranking;
import com.github.retro_game.retro_game.model.repository.*;
import com.github.retro_game.retro_game.service.RankingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RankingServiceImpl implements RankingService {
  private final BuildingsRankingRepository buildingsRankingRepository;
  private final DefenseRankingRepository defenseRankingRepository;
  private final FleetRankingRepository fleetRankingRepository;
  private final OverallRankingRepository overallRankingRepository;
  private final TechnologiesRankingRepository technologiesRankingRepository;

  public RankingServiceImpl(BuildingsRankingRepository buildingsRankingRepository,
                            DefenseRankingRepository defenseRankingRepository,
                            FleetRankingRepository fleetRankingRepository,
                            OverallRankingRepository overallRankingRepository,
                            TechnologiesRankingRepository technologiesRankingRepository) {
    this.buildingsRankingRepository = buildingsRankingRepository;
    this.defenseRankingRepository = defenseRankingRepository;
    this.fleetRankingRepository = fleetRankingRepository;
    this.overallRankingRepository = overallRankingRepository;
    this.technologiesRankingRepository = technologiesRankingRepository;
  }

  @Override
  public List<? extends Ranking> findAll(long bodyId, String kind) {
    if (kind == null) {
      return overallRankingRepository.findAll();
    }
    switch (kind) {
      case "buildings":
        return buildingsRankingRepository.findAll();
      case "defense":
        return defenseRankingRepository.findAll();
      case "fleet":
        return fleetRankingRepository.findAll();
      case "technologies":
        return technologiesRankingRepository.findAll();
    }
    return overallRankingRepository.findAll();
  }
}
