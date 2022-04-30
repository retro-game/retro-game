package com.github.retro_game.retro_game.service;

public interface CaptchaService {
  boolean verify(String response, String remoteAddr);
}
