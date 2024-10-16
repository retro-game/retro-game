package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PushEntryDto;

import java.util.ArrayList;
import java.util.List;

public interface PushDetectionService {
  List<ArrayList<PushEntryDto>> findPushes();
}
