package com.github.retro_game.retro_game.security;

import com.github.retro_game.retro_game.cache.BodyInfoCache;
import com.github.retro_game.retro_game.cache.UserInfoCache;
import com.github.retro_game.retro_game.repository.FreeSystemRepository;
import com.github.retro_game.retro_game.repository.UserRepository;
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

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
  private final BodyInfoCache bodyInfoCache;
  private final UserInfoCache userInfoCache;
  private final FreeSystemRepository freeSystemRepository;
  private final UserRepository userRepository;

  public CustomAuthenticationSuccessHandler(BodyInfoCache bodyInfoCache, UserInfoCache userInfoCache,
                                            FreeSystemRepository freeSystemRepository, UserRepository userRepository) {
    this.bodyInfoCache = bodyInfoCache;
    this.userInfoCache = userInfoCache;
    this.freeSystemRepository = freeSystemRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    var customUser = (CustomUser) authentication.getPrincipal();
    logger.info("User auth: userId={} ip={}", customUser.getUserId(), request.getRemoteAddr());

    var user = userRepository.getById(customUser.getUserId());
    var bodies = user.getBodies();

    // Evict cache.
    userInfoCache.evict(customUser.getUserId());
    for (var bodyId : bodies.keySet())
      bodyInfoCache.evict(bodyId);

    var homeworldId = bodies.keySet().stream().min(Long::compareTo);
    if (homeworldId.isPresent()) {
      response.sendRedirect(String.format("/overview?body=%d", homeworldId.get()));
      return;
    }
    var freeSystem = freeSystemRepository.findFirstBy();
    if (freeSystem == null) {
      throw new NoMoreFreeSystemsException();
    }
    response.sendRedirect(String.format("/create-homeworld?galaxy=%d&system=%d", freeSystem.getGalaxy(),
        freeSystem.getSystem()));
  }
}
