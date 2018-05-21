package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.EspionageReport;
import com.github.retro_game.retro_game.model.entity.EspionageReportSortOrder;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface EspionageReportRepositoryCustom {
  List<EspionageReport> findReports(User user, EspionageReportSortOrder order, Sort.Direction direction,
                                    Pageable pageable);
}
