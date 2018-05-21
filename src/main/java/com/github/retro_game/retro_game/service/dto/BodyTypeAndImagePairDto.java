package com.github.retro_game.retro_game.service.dto;

public class BodyTypeAndImagePairDto {
  private final BodyTypeDto type;
  private final int image;

  public BodyTypeAndImagePairDto(BodyTypeDto type, int image) {
    this.type = type;
    this.image = image;
  }

  public BodyTypeDto getType() {
    return type;
  }

  public int getImage() {
    return image;
  }
}
