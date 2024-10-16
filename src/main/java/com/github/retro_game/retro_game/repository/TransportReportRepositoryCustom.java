package com.github.retro_game.retro_game.repository;

import com.github.retro_game.retro_game.dto.TransportReportAndPointsDto;
import com.github.retro_game.retro_game.entity.TransportReport;
import com.github.retro_game.retro_game.entity.TransportReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TransportReportRepositoryCustom {
  List<TransportReport> findReports(User user, TransportReportSortOrder order, Sort.Direction direction,
                                    Pageable pageable);

  List<TransportReportAndPointsDto> findReportsForPushDetection();
}
