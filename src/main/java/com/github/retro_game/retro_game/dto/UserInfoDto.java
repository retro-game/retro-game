package com.github.retro_game.retro_game.dto;

import java.util.List;
import java.util.OptionalLong;

public class UserInfoDto {
  private final long id;
  private final String name;
  private final List<Long> bodiesIds;

  public UserInfoDto(long id, String name, List<Long> bodiesIds) {
    this.id = id;
    this.name = name;
    this.bodiesIds = bodiesIds;
  }

  public OptionalLong getHomeworldId() {
    return bodiesIds.stream().mapToLong(Long::longValue).min();
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Long> getBodiesIds() {
    return bodiesIds;
  }
}
