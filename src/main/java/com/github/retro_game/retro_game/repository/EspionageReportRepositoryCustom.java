package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.entity.EspionageReport;
import com.github.retro_game.retro_game.entity.EspionageReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface EspionageReportRepositoryCustom {
  List<EspionageReport> findReports(User user, EspionageReportSortOrder order, Sort.Direction direction,
                                    Pageable pageable);
}
