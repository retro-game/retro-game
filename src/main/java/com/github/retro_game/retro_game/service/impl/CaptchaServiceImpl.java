package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.GoogleRecaptchaResponseDto;
import com.github.retro_game.retro_game.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class CaptchaServiceImpl implements CaptchaService {
  private final String googleRecaptchaKeySecret;

  public CaptchaServiceImpl(@Value("${retro-game.google-recaptcha-key-secret}") String googleRecaptchaKeySecret) {
    this.googleRecaptchaKeySecret = googleRecaptchaKeySecret;
  }

  @Override
  public boolean verify(String response, String remoteAddr) {
    var verifyUri = URI.create(String.format(
        "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
        googleRecaptchaKeySecret, response, remoteAddr));
    var restTemplate = new RestTemplate();
    var recaptchaResponse = restTemplate.getForObject(verifyUri, GoogleRecaptchaResponseDto.class);
    return recaptchaResponse != null && recaptchaResponse.success();
  }
}
