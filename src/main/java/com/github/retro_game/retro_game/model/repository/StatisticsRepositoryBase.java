package com.github.retro_game.retro_game.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Date;
import java.util.List;

@NoRepositoryBean
public interface StatisticsRepositoryBase<T> extends JpaRepository<T, Long> {
  List<T> findByKey_At(Date at);

  @Query("select max(key.at) from #{#entityName}")
  Date getLastUpdatedAt();
}
