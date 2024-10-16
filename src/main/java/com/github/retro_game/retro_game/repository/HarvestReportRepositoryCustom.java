package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.HarvestReport;
import com.github.retro_game.retro_game.entity.HarvestReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface HarvestReportRepositoryCustom {
  List<HarvestReport> findReports(User user, HarvestReportSortOrder order, Sort.Direction direction, Pageable pageable);
}
