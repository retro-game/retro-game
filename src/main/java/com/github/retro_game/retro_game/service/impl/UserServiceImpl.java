package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.*;
import com.github.retro_game.retro_game.model.repository.EventRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.dto.UserSettingsDto;
import com.github.retro_game.retro_game.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service("userService")
class UserServiceImpl implements UserServiceInternal {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private final String defaultLanguage;
  private final String defaultSkin;
  private final PasswordEncoder passwordEncoder;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private ActivityService activityService;
  private BodyServiceInternal bodyServiceInternal;
  private FlightServiceInternal flightServiceInternal;
  private PrangerServiceInternal prangerServiceInternal;

  public UserServiceImpl(@Value("${retro-game.default-language}") String defaultLanguage,
                         @Value("${retro-game.default-skin}") String defaultSkin,
                         PasswordEncoder passwordEncoder,
                         EventRepository eventRepository,
                         UserRepository userRepository) {
    this.defaultLanguage = defaultLanguage;
    this.defaultSkin = defaultSkin;
    this.passwordEncoder = passwordEncoder;
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
  }

  @Autowired
  public void setActivityService(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Autowired
  public void setBodyServiceInternal(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Autowired
  public void setFlightServiceInternal(FlightServiceInternal flightServiceInternal) {
    this.flightServiceInternal = flightServiceInternal;
  }

  @Autowired
  public void setPrangerServiceInternal(PrangerServiceInternal prangerServiceInternal) {
    this.prangerServiceInternal = prangerServiceInternal;
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void create(String email, String name, String password) {
    // Make the first user an admin.
    int roles = UserRole.USER;
    if (userRepository.count() == 0) {
      roles |= UserRole.ADMIN;
    }

    int flags = UserFlag.NUMBER_INPUT_SCROLLING;

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    User user = new User();
    user.setName(name);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setRoles(roles);
    user.setMessagesSeenAt(now);
    user.setCombatReportsSeenAt(now);
    user.setEspionageReportsSeenAt(now);
    user.setHarvestReportsSeenAt(now);
    user.setTransportReportsSeenAt(now);
    user.setOtherReportsSeenAt(now);
    user.setLanguage(defaultLanguage);
    user.setSkin(defaultSkin);
    user.setBodiesSortOrder(BodiesSortOrder.EMERGENCE);
    user.setBodiesSortDirection(Sort.Direction.ASC);
    user.setNumProbes(1);
    user.setFlags(flags);
    user.setForcedVacation(false);
    userRepository.save(user);
  }

  @Override
  public boolean checkCurrentUserPassword(String password) {
    long userId = CustomUser.getCurrentUserId();
    String encodedPassword = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new).getPassword();
    return passwordEncoder.matches(password, encodedPassword);
  }

  @Override
  public boolean existsByEmailIgnoreCase(String email) {
    return userRepository.existsByEmailIgnoreCase(email);
  }

  @Override
  public boolean existsByNameIgnoreCase(String name) {
    return userRepository.existsByNameIgnoreCase(name);
  }

  @Override
  public long getCurrentId() {
    return CustomUser.getCurrentUserId();
  }

  @Override
  public String getName(long userId) {
    return userRepository.findById(userId).orElseThrow(UserDoesntExistException::new).getName();
  }

  @Override
  public String getCurrentUserName() {
    return getName(CustomUser.getCurrentUserId());
  }

  @Override
  public UserSettingsDto getCurrentUserSettings() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new);
    return new UserSettingsDto(user.getLanguage(), user.getSkin(), user.getNumProbes(),
        Converter.convert(user.getBodiesSortOrder()), user.getBodiesSortDirection(),
        user.hasFlag(UserFlag.NUMBER_INPUT_SCROLLING), user.hasFlag(UserFlag.SHOW_NEW_MESSAGES_IN_OVERVIEW),
        user.hasFlag(UserFlag.SHOW_NEW_REPORTS_IN_OVERVIEW), user.hasFlag(UserFlag.STICKY_MOONS));
  }

  @Override
  @Transactional
  // Evict bodiesBasicInfo, as the user may change the sort order of bodies and thus making the cached list invalid.
  // A better way would be to retrieve the cached value and then sort it, but the declarative nature of caching API in
  // Spring disallows it without using some hacks.
  @CacheEvict(cacheNames = "bodiesBasicInfo", key = "T(com.github.retro_game.retro_game.security.CustomUser).currentUserId")
  public void saveCurrentUserSettings(UserSettingsDto settings) {
    int flags = 0;
    if (settings.isNumberInputScrollingEnabled())
      flags |= UserFlag.NUMBER_INPUT_SCROLLING;
    if (settings.isShowNewMessagesInOverviewEnabled())
      flags |= UserFlag.SHOW_NEW_MESSAGES_IN_OVERVIEW;
    if (settings.isShowNewReportsInOverviewEnabled())
      flags |= UserFlag.SHOW_NEW_REPORTS_IN_OVERVIEW;
    if (settings.isStickyMoonsEnabled())
      flags |= UserFlag.STICKY_MOONS;

    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.findById(userId).orElseThrow(UserDoesntExistException::new);
    user.setLanguage(settings.getLanguage());
    user.setSkin(settings.getSkin());
    user.setNumProbes(settings.getNumProbes());
    user.setBodiesSortOrder(Converter.convert(settings.getBodiesSortOrder()));
    user.setBodiesSortDirection(settings.getBodiesSortDirection());
    user.setFlags(flags);
  }

  @Override
  public Optional<Locale> getCurrentUserLocaleIfAuthenticated() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUser)) {
      return Optional.empty();
    }
    UserSettingsDto settings = getCurrentUserSettings();
    Locale locale = new Locale(settings.getLanguage());
    return Optional.of(locale);
  }

  @Override
  public boolean isOnVacation() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return isOnVacation(user);
  }

  @Override
  public boolean isOnVacation(User user) {
    return user.getVacationUntil() != null;
  }

  @Override
  public Date getVacationUntil() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return user.getVacationUntil();
  }

  @Override
  public boolean canEnableVacationMode() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return canEnableVacationMode(user);
  }

  private boolean canEnableVacationMode(User user) {
    // Already on vacation?
    if (isOnVacation(user)) {
      return false;
    }

    // Check whether the user has sent some fleets or is targeted by someone else.
    if (flightServiceInternal.existsByUser(user)) {
      return false;
    }

    // All the following kinds of events take a body id as param. If an event exists, then an non-empty queue exists as
    // well, thus vacation mode cannot be enabled.
    List<EventKind> kinds = Arrays.asList(EventKind.BUILDING_QUEUE, EventKind.SHIPYARD_QUEUE,
        EventKind.TECHNOLOGY_QUEUE);
    Set<Long> ids = user.getBodies().keySet();
    return !eventRepository.existsByKindInAndParamIn(kinds, ids);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void enableVacationMode() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    if (!canEnableVacationMode(user)) {
      // A hacking attempt, the button should be disabled.
      logger.warn("Enabling vacation mode failed, requirements not met: userId={}", userId);
      throw new CannotEnableVacationModeException();
    }

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    Date until = Date.from(now.toInstant().plus(2, ChronoUnit.DAYS));

    logger.info("Enabling vacation mode: userId={} until='{}'", userId, until);
    enableVacationMode(user, now, until);
  }

  private void enableVacationMode(User user, Date now, Date until) {
    updateActivitiesAndBodies(user, now);
    user.setVacationUntil(until);
  }

  @Override
  public boolean canDisableVacationMode() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return canDisableVacationMode(user);
  }

  private boolean canDisableVacationMode(User user) {
    Date until = user.getVacationUntil();
    return until != null && !until.after(new Date());
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void disableVacationMode() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    if (!canDisableVacationMode(user)) {
      // A hacking attempt, the button should be disabled.
      logger.warn("Disabling vacation mode failed, requirements not met: userId={}", userId);
      throw new CannotDisableVacationModeException();
    }

    logger.info("Disabling vacation mode: userId={}", userId);

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    // Bodies must be updated before vacation until is set, otherwise resources will be calculated incorrectly.
    updateActivitiesAndBodies(user, now);
    user.setVacationUntil(null);
    user.setForcedVacation(false);
  }

  private void updateActivitiesAndBodies(User user, Date at) {
    long s = at.toInstant().getEpochSecond();

    for (Map.Entry<Long, Body> entry : user.getBodies().entrySet()) {
      // Update activity.
      long bodyId = entry.getKey();
      activityService.handleBodyActivity(bodyId, s);

      // Update resources.
      Body body = entry.getValue();
      bodyServiceInternal.updateResources(body, at);
    }
  }

  @Override
  public boolean isBanned() {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);
    return isBanned(user);
  }

  private boolean isBanned(User user) {
    return isBanned(user.getVacationUntil(), user.isForcedVacation());
  }

  @Override
  public boolean isBanned(Date vacationUntil, boolean forcedVacation) {
    return forcedVacation && vacationUntil != null && vacationUntil.after(new Date());
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void ban(String name, long durationDays, String reason) {
    long adminId = CustomUser.getCurrentUserId();
    User admin = userRepository.getOne(adminId);

    Optional<User> userOptional = userRepository.findByNameIgnoreCase(name);
    if (!userOptional.isPresent()) {
      logger.info("Banning user failed, user doesn't exist: adminId={}", adminId);
      throw new UserDoesntExistException();
    }
    User user = userOptional.get();

    if (isBanned(user)) {
      logger.info("Banning user failed, user is already banned: adminId={} userId={}", adminId, user.getId());
      throw new UserAlreadyBannedException();
    }

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    Date until = Date.from(now.toInstant().plus(durationDays, ChronoUnit.DAYS));

    logger.info("Banning user: adminId={} userId={} durationDays={} until='{}' reason='{}'", adminId, user.getId(),
        durationDays, until, reason);

    enableVacationMode(user, now, until);
    user.setForcedVacation(true);

    prangerServiceInternal.createEntry(user, now, until, reason, admin);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void unban(String name) {
    long adminId = CustomUser.getCurrentUserId();

    Optional<User> userOptional = userRepository.findByNameIgnoreCase(name);
    if (!userOptional.isPresent()) {
      logger.info("Unbanning user failed, user doesn't exist: adminId={}", adminId);
      throw new UserDoesntExistException();
    }
    User user = userOptional.get();

    if (!isBanned(user)) {
      logger.info("Unbanning user failed, user is not banned: adminId={} userId={}", adminId, user.getId());
      throw new UserNotBannedException();
    }

    logger.info("Unbanning user: adminId={} userId={}", adminId, user.getId());

    prangerServiceInternal.deleteEntry(user, user.getVacationUntil());

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
    user.setVacationUntil(now);
    user.setForcedVacation(false);
  }
}
