package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.CombatReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CombatReportRepository extends JpaRepository<CombatReport, UUID> {
  @Query(value = """
        select *
          from combat_reports
         where at < now() - cast(?1 as interval)
      order by (attackers_loss + defenders_loss) desc
         limit ?2
      """, nativeQuery = true)
  List<CombatReport> findTopReportsOrderByLoss(String interval, int num);

  @Query(value = """
        select *
          from combat_reports
         where at < now() - cast(?1 as interval)
      order by (plunder_metal + plunder_crystal + plunder_deuterium) desc
         limit ?2
      """, nativeQuery = true)
  List<CombatReport> findTopReportsOrderByPlunder(String interval, int num);

  @Query(value = """
        select *
          from combat_reports
         where at < now() - cast(?1 as interval)
      order by (debris_metal + debris_crystal) desc
         limit ?2
      """, nativeQuery = true)
  List<CombatReport> findTopReportsOrderByDebris(String interval, int num);
}
