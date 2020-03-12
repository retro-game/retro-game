package com.github.retro_game.retro_game.service;

import com.github.retro_game.retro_game.dto.PrangerEntryDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PrangerService {
  @PreAuthorize("hasPermission(#bodyId, 'ACCESS_BODY')")
  @Activity(bodies = "#bodyId")
  List<PrangerEntryDto> get(long bodyId);
}
