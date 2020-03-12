package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Date;
import java.util.List;

@NoRepositoryBean
public interface StatisticsRepositoryBase<T extends Statistics> extends JpaRepository<T, Long> {
  List<T> findByKey_At(Date at);

  @Query(value = "select * from #{#entityName} s where s.user_id = ?1 and s.at > now() - interval '1 week' order by " +
      "s.at", nativeQuery = true)
  List<T> getLastWeekByUserId(long userId);

  @Query(value = "select * from #{#entityName} s where s.user_id = ?1 and s.at > now() - interval '1 month' and " +
      "extract(hour from s.at) = 0 order by s.at", nativeQuery = true)
  List<T> getLastMonthByUserId(long userId);

  @Query("select s from #{#entityName} s where s.key.userId = ?1 and extract(dow from s.key.at) = 0 and " +
      "extract(hour from s.key.at) = 0 order by s.key.at")
  List<T> getAllTimeByUserId(long userId);

  @Query("select max(key.at) from #{#entityName}")
  Date getLastUpdatedAt();
}
