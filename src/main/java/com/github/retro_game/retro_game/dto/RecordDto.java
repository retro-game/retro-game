package com.github.retro_game.retro_game.dto;

import java.util.Date;
import java.util.List;

public record RecordDto(long value, Date at, List<String> holders, boolean isNew) {
}
