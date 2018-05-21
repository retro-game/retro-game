package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.model.entity.FreeSystem;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.FreeSystemRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.service.exception.NoMoreFreeSystemsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
  private final FreeSystemRepository freeSystemRepository;
  private final UserRepository userRepository;

  public CustomAuthenticationSuccessHandler(FreeSystemRepository freeSystemRepository, UserRepository userRepository) {
    this.freeSystemRepository = freeSystemRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    CustomUser customUser = (CustomUser) authentication.getPrincipal();
    logger.info("User auth: userId={} ip={}", customUser.getUserId(), request.getRemoteAddr());
    User user = userRepository.getOne(customUser.getUserId());
    Optional<Long> homeworldId = user.getBodies().keySet().stream().min(Long::compareTo);
    if (homeworldId.isPresent()) {
      response.sendRedirect(String.format("/overview?body=%d", homeworldId.get()));
      return;
    }
    FreeSystem freeSystem = freeSystemRepository.findFirstBy();
    if (freeSystem == null) {
      throw new NoMoreFreeSystemsException();
    }
    response.sendRedirect(String.format("/create-homeworld?galaxy=%d&system=%d", freeSystem.getGalaxy(),
        freeSystem.getSystem()));
  }
}
