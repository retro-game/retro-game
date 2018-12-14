package com.github.retro_game.retro_game.security;

import org.springframework.security.web.header.HeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CspHeaderWriter implements HeaderWriter {
  @Override
  public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
    String imgSrc;

    String uri = request.getRequestURI();
    if ("/alliance".equals(uri) || "/alliance/view".equals(uri)) {
      imgSrc = "*";
    } else {
      imgSrc = "'self'";
    }

    response.addHeader("Content-Security-Policy", String.format(
        "default-src 'none'; connect-src 'self'; img-src %s; script-src 'self'; style-src 'self'",
        imgSrc));
  }
}
