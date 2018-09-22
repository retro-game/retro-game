package com.github.retro_game.retro_game.service.impl.cache;

import com.github.retro_game.retro_game.model.entity.Statistics;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.*;
import com.github.retro_game.retro_game.service.dto.RankingDto;
import com.github.retro_game.retro_game.service.dto.RankingEntryDto;
import com.github.retro_game.retro_game.service.dto.RankingKindDto;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RankingCache {
  private final BuildingsStatisticsRepository buildingsStatisticsRepository;
  private final DefenseStatisticsRepository defenseStatisticsRepository;
  private final FleetStatisticsRepository fleetStatisticsRepository;
  private final OverallStatisticsRepository overallStatisticsRepository;
  private final TechnologiesStatisticsRepository technologiesStatisticsRepository;
  private final UserRepository userRepository;

  private static class Data {
    private final Date updatedAt;
    private final List<RankingEntryDto> overall;
    private final List<RankingEntryDto> buildings;
    private final List<RankingEntryDto> technologies;
    private final List<RankingEntryDto> fleet;
    private final List<RankingEntryDto> defense;

    private Data(Date updatedAt, List<RankingEntryDto> overall, List<RankingEntryDto> buildings,
                 List<RankingEntryDto> technologies, List<RankingEntryDto> fleet, List<RankingEntryDto> defense) {
      this.updatedAt = updatedAt;
      this.overall = overall;
      this.buildings = buildings;
      this.technologies = technologies;
      this.fleet = fleet;
      this.defense = defense;
    }
  }

  private Data data = new Data(Date.from(Instant.ofEpochSecond(0)), Collections.emptyList(), Collections.emptyList(),
      Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

  public RankingCache(BuildingsStatisticsRepository buildingsStatisticsRepository,
                      DefenseStatisticsRepository defenseStatisticsRepository,
                      FleetStatisticsRepository fleetStatisticsRepository,
                      OverallStatisticsRepository overallStatisticsRepository,
                      TechnologiesStatisticsRepository technologiesStatisticsRepository,
                      UserRepository userRepository) {
    this.buildingsStatisticsRepository = buildingsStatisticsRepository;
    this.defenseStatisticsRepository = defenseStatisticsRepository;
    this.fleetStatisticsRepository = fleetStatisticsRepository;
    this.overallStatisticsRepository = overallStatisticsRepository;
    this.technologiesStatisticsRepository = technologiesStatisticsRepository;
    this.userRepository = userRepository;
  }

  @PostConstruct
  private void initialUpdate() {
    Date lastUpdatedAt = overallStatisticsRepository.getLastUpdatedAt();
    update(lastUpdatedAt);
  }

  public void update(Date at) {
    Map<Long, String> names = userRepository.findAll().stream().collect(Collectors.toMap(User::getId, User::getName));
    List<RankingEntryDto> overall = createRanking(overallStatisticsRepository, at, names);
    List<RankingEntryDto> buildings = createRanking(buildingsStatisticsRepository, at, names);
    List<RankingEntryDto> technologies = createRanking(technologiesStatisticsRepository, at, names);
    List<RankingEntryDto> fleet = createRanking(fleetStatisticsRepository, at, names);
    List<RankingEntryDto> defense = createRanking(defenseStatisticsRepository, at, names);
    data = new Data(at, overall, buildings, technologies, fleet, defense);
  }

  private <T extends Statistics> List<RankingEntryDto> createRanking(StatisticsRepositoryBase<T> repository, Date at,
                                                                     Map<Long, String> names) {
    return Collections.unmodifiableList(repository.findByKey_At(at).stream()
        .map(e -> {
          long id = e.getUserId();
          String name = names.get(id);
          return new RankingEntryDto(id, name, e.getPoints(), e.getRank());
        })
        .sorted(Comparator.comparing(RankingEntryDto::getRank))
        .collect(Collectors.toList()));
  }

  public RankingDto getLatest(@Nullable RankingKindDto kind) {
    Data d = data;
    if (kind == null) {
      kind = RankingKindDto.OVERALL;
    }
    List<RankingEntryDto> entries;
    switch (kind) {
      case OVERALL:
      default:
        entries = d.overall;
        break;
      case BUILDINGS:
        entries = d.buildings;
        break;
      case TECHNOLOGIES:
        entries = d.technologies;
        break;
      case FLEET:
        entries = d.fleet;
        break;
      case DEFENSE:
        entries = d.defense;
        break;
    }
    return new RankingDto(d.updatedAt, entries);
  }
}
