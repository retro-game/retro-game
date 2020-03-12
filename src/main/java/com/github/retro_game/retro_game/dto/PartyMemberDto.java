package com.github.retro_game.retro_game.dto;

public class PartyMemberDto {
  private final long id;
  private final String name;

  public PartyMemberDto(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
