package com.github.retro_game.retro_game.config;

import com.github.retro_game.retro_game.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private final AuthenticationSuccessHandler authenticationSuccessHandler;
  private final CustomUserDetailsService customUserDetailsService;

  public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler,
                        CustomUserDetailsService customUserDetailsService) {
    this.authenticationSuccessHandler = authenticationSuccessHandler;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
      .authorizeRequests()
        .antMatchers(
            "/",
            "/combat-report",
            "/espionage-report",
            "/join",
            "/static/**").permitAll()
        .anyRequest().authenticated()
        .and()
      .csrf()
        .ignoringAntMatchers(
            "/flights/send-probes",
            "/messages/delete",
            "/reports/combat/delete",
            "/reports/espionage/delete",
            "/reports/harvest/delete",
            "/reports/transport/delete",
            "/reports/other/delete")
        .and()
      .formLogin()
        .loginPage("/")
        .usernameParameter("email")
        .successHandler(authenticationSuccessHandler)
        .and()
      .headers()
        .contentSecurityPolicy("default-src 'none'; connect-src 'self'; img-src 'self'; script-src 'self'; style-src 'self'").and()
        .frameOptions().deny()
        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER);
    // @formatter:on
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
