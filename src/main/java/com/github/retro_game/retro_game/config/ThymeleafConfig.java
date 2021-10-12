package com.github.retro_game.retro_game.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ThymeleafConfig {
  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }
}
