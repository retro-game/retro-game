package com.github.retro_game.retro_game.config;

import com.github.retro_game.retro_game.security.CspHeaderWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security configuration.
 *
 * <p>Spring Security 6 removed {@code WebSecurityConfigurerAdapter}: security is
 * now expressed as a {@link SecurityFilterChain} bean built with the lambda DSL.
 * Authentication is wired automatically from the {@code CustomUserDetailsService}
 * and the {@link PasswordEncoder} bean below, so no explicit AuthenticationManager
 * setup is needed.
 */
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity replaces @EnableGlobalMethodSecurity; pre/post annotations are on by default.
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 @Value("${retro-game.enable-join-captcha}") boolean enableJoinCaptcha,
                                                 AuthenticationSuccessHandler authenticationSuccessHandler)
      throws Exception {
    // @formatter:off
    http
        .authorizeHttpRequests(authorize -> authorize
            // Public
            .requestMatchers(
                "/",
                "/combat-report",
                "/espionage-report",
                "/join",
                "/reset-password",
                "/change-password",
                "/static/**").permitAll()
            // Admin
            .requestMatchers("/admin/**").hasRole("ADMIN")
            // Vacation mode — these actions are blocked while the user is on vacation
            .requestMatchers(HttpMethod.POST, "/body-settings/abandon").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/buildings/*").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/flights/*").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/jump-gate/jump").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/party/*").access(notOnVacation())
            .requestMatchers(HttpMethod.GET, "/phalanx").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/shipyard/build").access(notOnVacation())
            .requestMatchers(HttpMethod.POST, "/technologies/*").access(notOnVacation())
            // Other
            .anyRequest().authenticated())
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
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
                "/reports/other/delete-all"))
        .formLogin(form -> form
            .loginPage("/")
            .usernameParameter("email")
            .successHandler(authenticationSuccessHandler))
        .headers(headers -> headers
            .addHeaderWriter(new CspHeaderWriter(enableJoinCaptcha))
            .frameOptions(frameOptions -> frameOptions.deny())
            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));
    // @formatter:on
    return http.build();
  }

  /**
   * Authorization rule that permits the request only when the current user is not on vacation.
   * The SpEL expression resolves the {@code userService} bean at request time.
   */
  private static WebExpressionAuthorizationManager notOnVacation() {
    return new WebExpressionAuthorizationManager("!@userService.isOnVacation()");
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Wires {@code CustomPermissionEvaluator} into method security explicitly.
   * Spring Security 6's @EnableMethodSecurity no longer auto-detects a
   * PermissionEvaluator bean, so without this the {@code hasPermission(...)}
   * checks in @PreAuthorize would always deny. The bean is static so it is
   * created early, before the beans it would otherwise trigger initialization of.
   */
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator permissionEvaluator) {
    var handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}
