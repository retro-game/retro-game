package com.github.retro_game.retro_game.model.repository;

import com.github.retro_game.retro_game.model.entity.TransportReport;
import com.github.retro_game.retro_game.model.entity.TransportReportSortOrder;
import com.github.retro_game.retro_game.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TransportReportRepositoryCustom {
  List<TransportReport> findReports(User user, TransportReportSortOrder order, Sort.Direction direction,
                                    Pageable pageable);
}
