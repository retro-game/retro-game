package com.github.retro_game.retro_game.security;

import org.springframework.security.web.header.HeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CspHeaderWriter implements HeaderWriter {
  private final boolean enableJoinCaptcha;

  public CspHeaderWriter(boolean enableJoinCaptcha) {
    this.enableJoinCaptcha = enableJoinCaptcha;
  }

  @Override
  public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
    var uri = request.getRequestURI();

    String frameSrc, scriptSrc;
    if (enableJoinCaptcha && "/join".equals(uri)) {
      frameSrc = "https://www.google.com/recaptcha/ https://recaptcha.google.com/recaptcha/";
      scriptSrc = "'self' https://www.google.com/recaptcha/ https://www.gstatic.com/recaptcha/";
    } else {
      frameSrc = "'none'";
      scriptSrc = "'self'";
    }

    String imgSrc;
    if ("/alliance".equals(uri) || "/alliance/view".equals(uri)) {
      imgSrc = "*";
    } else {
      imgSrc = "'self'";
    }

    response.addHeader("Content-Security-Policy", String.format(
        "default-src 'none'; connect-src 'self'; frame-src %s; img-src %s; script-src %s; style-src 'self'",
        frameSrc, imgSrc, scriptSrc));
  }
}
