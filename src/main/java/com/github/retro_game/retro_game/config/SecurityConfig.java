package com.github.retro_game.retro_game.config;

import com.github.retro_game.retro_game.security.CspHeaderWriter;
import com.github.retro_game.retro_game.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        // Public
        .antMatchers(
            "/",
            "/combat-report",
            "/espionage-report",
            "/join",
            "/static/**").permitAll()
        // Admin
        .antMatchers("/admin/**").hasRole("ADMIN")
        // Vacation mode
        .antMatchers(HttpMethod.POST, "/body-settings/abandon").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/buildings/*").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/flights/*").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/jump-gate/jump").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/party/*").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.GET, "/phalanx").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/shipyard/build").access("!@userService.isOnVacation()")
        .antMatchers(HttpMethod.POST, "/technologies/*").access("!@userService.isOnVacation()")
        // Other
        .anyRequest().authenticated()
        .and()
      .csrf()
        .ignoringAntMatchers(
            "/flights/send-probes",
            "/messages/private/delete",
            "/messages/private/delete-all",
            "/reports/combat/delete",
            "/reports/combat/delete-all",
            "/reports/espionage/delete",
            "/reports/espionage/delete-all",
            "/reports/harvest/delete",
            "/reports/harvest/delete-all",
            "/reports/transport/delete",
            "/reports/transport/delete-all",
            "/reports/other/delete",
            "/reports/other/delete-all")
        .and()
      .formLogin()
        .loginPage("/")
        .usernameParameter("email")
        .successHandler(authenticationSuccessHandler)
        .and()
      .headers()
        .addHeaderWriter(new CspHeaderWriter())
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
