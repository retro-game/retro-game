package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.RecordDto;

import java.util.Map;

public interface RecordsService {
  Map<String, RecordDto> getRecords();

  void share(long bodyId, boolean buildings, boolean technologies, boolean units, boolean production, boolean other);
}
