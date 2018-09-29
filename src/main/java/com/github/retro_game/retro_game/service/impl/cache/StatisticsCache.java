package com.github.retro_game.retro_game.service.impl.cache;

import com.github.retro_game.retro_game.model.entity.Statistics;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.*;
import com.github.retro_game.retro_game.service.dto.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StatisticsCache {
  private final BuildingsStatisticsRepository buildingsStatisticsRepository;
  private final DefenseStatisticsRepository defenseStatisticsRepository;
  private final FleetStatisticsRepository fleetStatisticsRepository;
  private final OverallStatisticsRepository overallStatisticsRepository;
  private final TechnologiesStatisticsRepository technologiesStatisticsRepository;
  private final UserRepository userRepository;

  private static class Data {
    private final Date updatedAt;
    private final Map<Long, StatisticsSummaryDto> usersSummaries;
    private final List<RankingEntryDto> overallRanking;
    private final List<RankingEntryDto> buildingsRanking;
    private final List<RankingEntryDto> technologiesRanking;
    private final List<RankingEntryDto> fleetRanking;
    private final List<RankingEntryDto> defenseRanking;

    private Data(Date updatedAt, Map<Long, StatisticsSummaryDto> usersSummaries, List<RankingEntryDto> overallRanking,
                 List<RankingEntryDto> buildingsRanking, List<RankingEntryDto> technologiesRanking,
                 List<RankingEntryDto> fleetRanking, List<RankingEntryDto> defenseRanking) {
      this.updatedAt = updatedAt;
      this.usersSummaries = usersSummaries;
      this.overallRanking = overallRanking;
      this.buildingsRanking = buildingsRanking;
      this.technologiesRanking = technologiesRanking;
      this.fleetRanking = fleetRanking;
      this.defenseRanking = defenseRanking;
    }
  }

  private Data data = new Data(Date.from(Instant.ofEpochSecond(0)), Collections.emptyMap(), Collections.emptyList(),
      Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

  public StatisticsCache(BuildingsStatisticsRepository buildingsStatisticsRepository,
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
    Map<Long, PointsAndRankPairDto> overallStatistics = fetchStatistics(overallStatisticsRepository, at);
    Map<Long, PointsAndRankPairDto> buildingsStatistics = fetchStatistics(buildingsStatisticsRepository, at);
    Map<Long, PointsAndRankPairDto> technologiesStatistics = fetchStatistics(technologiesStatisticsRepository, at);
    Map<Long, PointsAndRankPairDto> fleetStatistics = fetchStatistics(fleetStatisticsRepository, at);
    Map<Long, PointsAndRankPairDto> defenseStatistics = fetchStatistics(defenseStatisticsRepository, at);

    Map<Long, StatisticsSummaryDto> usersSummaries = Collections.unmodifiableMap(overallStatistics.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> {
              long id = e.getKey();
              return new StatisticsSummaryDto(e.getValue(), buildingsStatistics.get(id), technologiesStatistics.get(id),
                  fleetStatistics.get(id), defenseStatistics.get(id));
            }
        )));

    Map<Long, String> names = userRepository.findAll().stream().collect(Collectors.toMap(User::getId, User::getName));

    List<RankingEntryDto> overallRanking = createRanking(overallStatistics, names);
    List<RankingEntryDto> buildingsRanking = createRanking(buildingsStatistics, names);
    List<RankingEntryDto> technologiesRanking = createRanking(technologiesStatistics, names);
    List<RankingEntryDto> fleetRanking = createRanking(fleetStatistics, names);
    List<RankingEntryDto> defenseRanking = createRanking(defenseStatistics, names);

    data = new Data(at, usersSummaries, overallRanking, buildingsRanking, technologiesRanking, fleetRanking,
        defenseRanking);

  }

  private <T extends Statistics> Map<Long, PointsAndRankPairDto> fetchStatistics(StatisticsRepositoryBase<T> repository,
                                                                                 Date at) {
    return repository.findByKey_At(at).stream()
        .collect(Collectors.toMap(
            T::getUserId,
            e -> new PointsAndRankPairDto(e.getPoints(), e.getRank())
        ));
  }

  private List<RankingEntryDto> createRanking(Map<Long, PointsAndRankPairDto> statistics, Map<Long, String> names) {
    return Collections.unmodifiableList(statistics.entrySet().stream()
        .map(e -> {
          long id = e.getKey();
          PointsAndRankPairDto pair = e.getValue();
          return new RankingEntryDto(id, names.get(id), pair.getPoints(), pair.getRank());
        })
        .sorted(Comparator.comparing(RankingEntryDto::getRank))
        .collect(Collectors.toList()));
  }

  @Nullable
  public StatisticsSummaryDto getUserSummary(long userId) {
    return data.usersSummaries.get(userId);
  }

  public RankingDto getLatestRanking(StatisticsKindDto kind) {
    Data d = data;
    List<RankingEntryDto> entries;
    switch (kind) {
      case OVERALL:
      default:
        entries = d.overallRanking;
        break;
      case BUILDINGS:
        entries = d.buildingsRanking;
        break;
      case TECHNOLOGIES:
        entries = d.technologiesRanking;
        break;
      case FLEET:
        entries = d.fleetRanking;
        break;
      case DEFENSE:
        entries = d.defenseRanking;
        break;
    }
    return new RankingDto(d.updatedAt, entries);
  }
}
