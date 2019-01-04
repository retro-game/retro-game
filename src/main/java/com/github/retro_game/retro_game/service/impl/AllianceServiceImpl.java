package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.*;
import com.github.retro_game.retro_game.model.repository.AllianceApplicationRepository;
import com.github.retro_game.retro_game.model.repository.AllianceMemberRepository;
import com.github.retro_game.retro_game.model.repository.AllianceRepository;
import com.github.retro_game.retro_game.model.repository.UserRepository;
import com.github.retro_game.retro_game.security.CustomUser;
import com.github.retro_game.retro_game.service.AllianceService;
import com.github.retro_game.retro_game.service.dto.*;
import com.github.retro_game.retro_game.service.exception.*;
import com.github.retro_game.retro_game.service.impl.cache.AllianceTagCache;
import com.github.retro_game.retro_game.service.impl.cache.UserAllianceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class AllianceServiceImpl implements AllianceService {
  private static final Logger logger = LoggerFactory.getLogger(AllianceServiceImpl.class);
  private final AllianceRepository allianceRepository;
  private final AllianceApplicationRepository allianceApplicationRepository;
  private final AllianceMemberRepository allianceMemberRepository;
  private final UserRepository userRepository;
  private final AllianceTagCache allianceTagCache;
  private final UserAllianceCache userAllianceCache;
  private MessageServiceInternal messageServiceInternal;
  private UserServiceInternal userServiceInternal;

  public AllianceServiceImpl(AllianceRepository allianceRepository,
                             AllianceApplicationRepository allianceApplicationRepository,
                             AllianceMemberRepository allianceMemberRepository, UserRepository userRepository,
                             AllianceTagCache allianceTagCache, UserAllianceCache userAllianceCache) {
    this.allianceRepository = allianceRepository;
    this.allianceApplicationRepository = allianceApplicationRepository;
    this.allianceMemberRepository = allianceMemberRepository;
    this.userRepository = userRepository;
    this.allianceTagCache = allianceTagCache;
    this.userAllianceCache = userAllianceCache;
  }

  @Autowired
  public void setMessageServiceInternal(MessageServiceInternal messageServiceInternal) {
    this.messageServiceInternal = messageServiceInternal;
  }

  @Autowired
  public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
    this.userServiceInternal = userServiceInternal;
  }

  private void joinAlliance(Alliance alliance, User user) {
    AllianceMemberKey key = new AllianceMemberKey();
    key.setAlliance(alliance);
    key.setUser(user);

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    AllianceMember member = new AllianceMember();
    member.setKey(key);
    member.setRank(null);
    member.setJoinedAt(now);
    allianceMemberRepository.save(member);

    // Update cache.
    long allianceId = alliance.getId();
    long userId = user.getId();
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        userAllianceCache.updateUserAlliance(userId, allianceId);
      }
    });
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void create(long bodyId, String tag, String name) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    if (allianceMemberRepository.existsByKey_User(user)) {
      logger.warn("Creating alliance failed, user already has an alliance: userId={}", userId);
      throw new UserAlreadyHasAllianceException();
    }

    if (allianceApplicationRepository.existsByUser(user)) {
      logger.warn("Creating alliance failed, user is already applying to some alliance: userId={}", userId);
      throw new UserIsAlreadyApplyingToAllianceException();
    }

    if (allianceRepository.existsByTagIgnoreCase(tag)) {
      throw new AllianceTagAlreadyExistsException();
    }

    if (allianceRepository.existsByNameIgnoreCase(name)) {
      throw new AllianceNameAlreadyExistsException();
    }

    Alliance alliance = new Alliance();
    alliance.setOwner(user);
    alliance.setTag(tag);
    alliance.setName(name);
    alliance.setRecruitmentOpen(true);
    alliance.setLogo(null);
    alliance.setExternalText("");
    alliance.setInternalText("");
    alliance.setApplicationText("");
    allianceRepository.save(alliance);

    joinAlliance(alliance, user);

    long allianceId = alliance.getId();

    logger.info("Creating alliance: userId={} allianceId={} tag='{}'", userId, allianceId, tag);

    // Update cache.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        allianceTagCache.updateTag(allianceId, tag);
      }
    });
  }

  private static int getPrivileges(User user, Alliance alliance, AllianceMember member) {
    // Admins and owners have every privilege.
    if (user.hasRole(UserRole.ADMIN) || user.getId() == alliance.getOwner().getId())
      return ~0;

    // Not a member, thus no privileges.
    if (member == null)
      return 0;

    /*
    AllianceRank rank = member.getRank();
    // The user doesn't have an assigned rank (newly accepted user), thus no privileges.
    if (rank == null)
      return 0;

    // The user has a concrete rank, get the set privileges.
    return rank.getPrivileges();
    */

    // FIXME: Temporary solution until proper rank implementation.
    int privileges = 1 << AlliancePrivilege.WRITE_CIRCULAR_MESSAGE.ordinal();
    privileges |= 1 << AlliancePrivilege.SHOW_MEMBER_LIST.ordinal();
    return privileges;
  }

  private static boolean hasPrivilege(int privileges, AlliancePrivilege privilege) {
    return (privileges & (1 << privilege.ordinal())) != 0;
  }

  private static boolean canSeeInternalText(User user, @Nullable AllianceMember member) {
    boolean own = member != null;
    return own || user.hasRole(UserRole.ADMIN);
  }

  private AllianceMember getAllianceMember(Alliance alliance, User user) {
    AllianceMemberKey key = new AllianceMemberKey();
    key.setAlliance(alliance);
    key.setUser(user);
    return allianceMemberRepository.findById(key).orElse(null);
  }

  private static class UserAndAllianceAndMemberTuple {
    private final User user;
    private final Alliance alliance;
    private final AllianceMember member;

    private UserAndAllianceAndMemberTuple(User user, Alliance alliance, AllianceMember member) {
      this.user = user;
      this.alliance = alliance;
      this.member = member;
    }
  }

  private UserAndAllianceAndMemberTuple getUserAndAllianceAndMember(long allianceId) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Alliance> optionalAlliance = allianceRepository.findById(allianceId);
    if (!optionalAlliance.isPresent()) {
      logger.warn("Getting alliance failed, the alliance does not exist: userId={} allianceId={}", userId, allianceId);
      throw new AllianceDoesNotExistException();
    }
    Alliance alliance = optionalAlliance.get();

    User user = userRepository.getOne(userId);
    AllianceMember member = getAllianceMember(alliance, user);
    return new UserAndAllianceAndMemberTuple(user, alliance, member);
  }

  private AllianceDto get(User user, Alliance alliance, @Nullable AllianceMember member) {
    Long ownerId = null;
    String ownerName = null;
    User owner = alliance.getOwner();
    if (owner != null) {
      ownerId = owner.getId();
      ownerName = owner.getName();
    }

    boolean own = member != null;
    boolean applyLinkVisible = !own && alliance.isRecruitmentOpen();
    boolean internalTextVisible = canSeeInternalText(user, member);
    boolean leaveLinkVisible = own && (ownerId == null || user.getId() != ownerId);

    int privileges = getPrivileges(user, alliance, member);
    boolean applicationsLinkVisible = hasPrivilege(privileges, AlliancePrivilege.SHOW_APPLICATIONS);
    boolean memberListLinkVisible = hasPrivilege(privileges, AlliancePrivilege.SHOW_MEMBER_LIST);
    boolean circularMessageLinkVisible = hasPrivilege(privileges, AlliancePrivilege.WRITE_CIRCULAR_MESSAGE);
    boolean manageLinkVisible = hasPrivilege(privileges, AlliancePrivilege.MANAGE_ALLIANCE);

    int numMembers = (int) allianceMemberRepository.countByKey_Alliance(alliance);

    int numApplications = 0;
    if (applicationsLinkVisible) {
      // Query only when necessary, as the applications link for most of the viewers is not visible.
      numApplications = (int) allianceApplicationRepository.countByAlliance(alliance);
    }

    return new AllianceDto(alliance.getId(), ownerId, ownerName, alliance.getTag(), alliance.getName(),
        alliance.isRecruitmentOpen(), alliance.getLogo(), alliance.getExternalText(), alliance.getInternalText(),
        numMembers, numApplications, applyLinkVisible, internalTextVisible, applicationsLinkVisible,
        memberListLinkVisible, circularMessageLinkVisible, manageLinkVisible, leaveLinkVisible);
  }

  @Override
  public AllianceDto getById(long bodyId, long allianceId) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);
    return get(tuple.user, tuple.alliance, tuple.member);
  }

  @Override
  @Nullable
  public AllianceDto getCurrentUserAlliance(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Optional<AllianceMember> optionalMember = allianceMemberRepository.findByKey_User(user);
    if (!optionalMember.isPresent()) {
      // The user doesn't have an alliance.
      return null;
    }
    AllianceMember member = optionalMember.get();

    return get(user, member.getAlliance(), member);
  }

  @Override
  public String getText(long bodyId, long allianceId, AllianceTextKindDto kind) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Alliance> optionalAlliance = allianceRepository.findById(allianceId);
    if (!optionalAlliance.isPresent()) {
      logger.warn("Getting alliance text failed, the alliance does not exist: userId={} allianceId={}", userId,
          allianceId);
      throw new AllianceDoesNotExistException();
    }
    Alliance alliance = optionalAlliance.get();

    assert kind != null;
    if (kind == AllianceTextKindDto.EXTERNAL)
      return alliance.getExternalText();
    if (kind == AllianceTextKindDto.APPLICATION)
      return alliance.getApplicationText();
    assert kind == AllianceTextKindDto.INTERNAL;

    User user = userRepository.getOne(userId);
    AllianceMember member = getAllianceMember(alliance, user);
    if (!canSeeInternalText(user, member)) {
      logger.warn("Getting alliance internal text failed, unauthorized access: userId={} allianceId={}", userId,
          allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    return alliance.getInternalText();
  }

  @Override
  public List<AllianceMemberDto> getMembers(long bodyId, long allianceId) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);

    int privileges = getPrivileges(tuple.user, tuple.alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.SHOW_MEMBER_LIST)) {
      logger.warn("Getting the alliance member list failed, unauthorized access: userId={} allianceId={}",
          tuple.user.getId(), allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    return tuple.alliance.getMembers().stream()
        .map(m -> {
          User u = m.getUser();
          AllianceRank rank = m.getRank();
          String rankName = rank == null ? null : rank.getName();
          return new AllianceMemberDto(u.getId(), u.getName(), m.getJoinedAt(), rankName);
        })
        .sorted(Comparator.comparing(AllianceMemberDto::getJoinedAt))
        .collect(Collectors.toList());
  }

  @Override
  public void sendCircularMessage(long bodyId, long allianceId, String message) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);

    int privileges = getPrivileges(tuple.user, tuple.alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.WRITE_CIRCULAR_MESSAGE)) {
      logger.warn("Sending circular message failed, unauthorized access: userId={} allianceId={}", tuple.user.getId(),
          allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    logger.info("Sending circular message successful: userId={} allianceId={}", tuple.user.getId(), allianceId);

    String msg = "--- ALLIANCE MESSAGE ---\n" + message;

    List<User> recipients = tuple.alliance.getMembers().stream()
        .map(AllianceMember::getUser)
        .collect(Collectors.toList());

    messageServiceInternal.sendToMultipleUsers(recipients, msg);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void leave(long bodyId, long allianceId) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);
    long userId = tuple.user.getId();

    if (tuple.alliance.getOwner().getId() == userId) {
      logger.warn("Leaving alliance failed, the owner cannot leave: userId={} allianceId={}", userId, allianceId);
      throw new OwnerCannotLeaveException();
    }

    AllianceMember member = tuple.member;
    if (member == null) {
      logger.warn("Leaving alliance failed, user is not a member of the alliance: userId={} allianceId={}",
          userId, allianceId);
      throw new UserIsNotAMemberException();
    }

    logger.info("Leaving alliance successful: userId={} allianceId={}", userId, allianceId);
    allianceMemberRepository.delete(member);

    // Update cache.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        userAllianceCache.removeUserAlliance(userId);
      }
    });
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void apply(long bodyId, long allianceId, String applicationText) {
    long userId = CustomUser.getCurrentUserId();

    Optional<Alliance> optionalAlliance = allianceRepository.findById(allianceId);
    if (!optionalAlliance.isPresent()) {
      logger.warn("Applying to an alliance failed, the alliance does not exist: userId={} allianceId={}", userId,
          allianceId);
      throw new AllianceDoesNotExistException();
    }
    Alliance alliance = optionalAlliance.get();

    if (!alliance.isRecruitmentOpen()) {
      logger.warn("Applying to an alliance failed, recruitment is closed: userId={} allianceId={}", userId, allianceId);
      throw new AllianceRecruitmentIsClosedException();
    }

    User user = userRepository.getOne(userId);

    if (allianceMemberRepository.existsByKey_User(user)) {
      logger.warn("Applying to an alliance failed, user already has an alliance: userId={} allianceId={}", userId,
          allianceId);
      throw new UserAlreadyHasAllianceException();
    }

    if (allianceApplicationRepository.existsByUser(user)) {
      logger.warn("Applying to an alliance failed, user is already applying to some alliance: userId={} allianceId={}",
          userId, allianceId);
      throw new UserIsAlreadyApplyingToAllianceException();
    }

    Date now = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

    AllianceApplication application = new AllianceApplication();
    application.setAlliance(alliance);
    application.setUser(user);
    application.setAt(now);
    application.setApplicationText(applicationText);
    allianceApplicationRepository.save(application);

    logger.info("Applying to an alliance successful: userId={} allianceId={} applicationId={}", userId, allianceId,
        application.getId());
  }

  @Override
  @Nullable
  public AllianceApplicationDto getCurrentUserApplication(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Optional<AllianceApplication> optionalApplication = allianceApplicationRepository.findByUser(user);
    if (!optionalApplication.isPresent()) {
      return null;
    }
    AllianceApplication application = optionalApplication.get();

    Alliance alliance = application.getAlliance();
    return new AllianceApplicationDto(application.getId(), alliance.getId(), alliance.getTag(), userId, user.getName(),
        application.getAt(), application.getApplicationText());
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void cancelCurrentUserApplication(long bodyId) {
    long userId = CustomUser.getCurrentUserId();
    User user = userRepository.getOne(userId);

    Optional<AllianceApplication> optionalApplication = allianceApplicationRepository.findByUser(user);
    if (!optionalApplication.isPresent()) {
      logger.warn("Canceling the alliance applications failed, the application does not exist: userId={}", userId);
      throw new AllianceApplicationDoesNotExistException();
    }

    allianceApplicationRepository.delete(optionalApplication.get());
  }

  @Override
  public AllianceApplicationListDto getApplications(long bodyId, long allianceId) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);

    int privileges = getPrivileges(tuple.user, tuple.alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.SHOW_APPLICATIONS)) {
      logger.warn("Getting the alliance applications failed, unauthorized access: userId={} allianceId={}",
          tuple.user.getId(), allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    List<AllianceApplicationDto> applications =
        allianceApplicationRepository.findByAllianceOrderByAtDesc(tuple.alliance)
            .stream()
            .map(a -> {
              User u = a.getUser();
              return new AllianceApplicationDto(a.getId(), tuple.alliance.getId(), tuple.alliance.getTag(), u.getId(),
                  u.getName(), a.getAt(), a.getApplicationText());
            })
            .collect(Collectors.toList());
    boolean processable = hasPrivilege(privileges, AlliancePrivilege.PROCESS_APPLICATIONS);
    return new AllianceApplicationListDto(applications, processable);
  }

  private void processApplication(long applicationId, boolean accept) {
    long userId = CustomUser.getCurrentUserId();

    Optional<AllianceApplication> optionalApplication = allianceApplicationRepository.findById(applicationId);
    if (!optionalApplication.isPresent()) {
      logger.warn("Processing application failed, the application doesn't exist: userId={} applicationId={}",
          userId, applicationId);
      throw new AllianceApplicationDoesNotExistException();
    }
    AllianceApplication application = optionalApplication.get();

    Alliance alliance = application.getAlliance();
    User recruit = application.getUser();

    User recruiter = userRepository.getOne(userId);
    AllianceMember recruiterMember = getAllianceMember(alliance, recruiter);

    int privileges = getPrivileges(recruiter, alliance, recruiterMember);
    if (!hasPrivilege(privileges, AlliancePrivilege.PROCESS_APPLICATIONS)) {
      logger.warn(
          "Processing application failed, unauthorized access: userId={} applicationId={} allianceId={} recruitId={}",
          userId, applicationId, alliance.getId(), recruit.getId());
      throw new UnauthorizedAllianceAccessException();
    }

    if (accept) {
      logger.info("Processing application successful, accepting the application: userId={} applicationId={} " +
              "allianceId={} recruitId={}",
          userId, applicationId, alliance.getId(), recruit.getId());
      joinAlliance(alliance, recruit);
    } else {
      logger.info("Processing application successful, rejecting the application: userId={} applicationId={} " +
              "allianceId={} recruitId={}",
          userId, applicationId, alliance.getId(), recruit.getId());
    }

    allianceApplicationRepository.delete(application);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void acceptApplication(long bodyId, long applicationId) {
    processApplication(applicationId, true);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void rejectApplication(long bodyId, long applicationId) {
    processApplication(applicationId, false);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void kickUser(long bodyId, long allianceId, long userIdToKick) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);

    int privileges = getPrivileges(tuple.user, tuple.alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.KICK_USER)) {
      logger.warn("Kicking user from alliance failed, unauthorized access: userId={} allianceId={} userIdToKick={}",
          tuple.user.getId(), allianceId, userIdToKick);
      throw new UnauthorizedAllianceAccessException();
    }

    Optional<User> optionalUserToKick = userRepository.findById(userIdToKick);
    if (!optionalUserToKick.isPresent()) {
      logger.warn("Kicking user from alliance failed, user to be kicked does not exist: userId={} allianceId={} " +
              "userIdToKick={}",
          tuple.user.getId(), allianceId, userIdToKick);
      throw new UserDoesntExistException();
    }
    User userToKick = optionalUserToKick.get();

    if (tuple.alliance.getOwner().getId() == userIdToKick) {
      logger.warn("Kicking user from alliance failed, cannot kick the owner: userId={} allianceId={} userIdToKick={}",
          tuple.user.getId(), allianceId, userIdToKick);
      throw new CannotKickOwnerException();
    }

    AllianceMember memberToKick = getAllianceMember(tuple.alliance, userToKick);
    if (memberToKick == null) {
      logger.warn("Kicking user from alliance failed, user to be kicked is not a member of that alliance: userId={} " +
              "allianceId={} userIdToKick={}",
          tuple.user.getId(), allianceId, userIdToKick);
      throw new UserIsNotAMemberException();
    }

    logger.info("Kicking user from alliance successful: userId={} allianceId={} kickedUserId={}", tuple.user.getId(),
        allianceId, userIdToKick);
    allianceMemberRepository.delete(memberToKick);

    // Update cache.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        userAllianceCache.removeUserAlliance(userIdToKick);
      }
    });
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void saveLogo(long bodyId, long allianceId, String url) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);
    Alliance alliance = tuple.alliance;

    int privileges = getPrivileges(tuple.user, alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.MANAGE_ALLIANCE)) {
      logger.warn("Saving alliance logo failed, unauthorized access: userId={} allianceId={}", tuple.user.getId(),
          allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    alliance.setLogo(url);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void saveText(long bodyId, long allianceId, AllianceTextKindDto kind, String text) {
    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);
    Alliance alliance = tuple.alliance;

    int privileges = getPrivileges(tuple.user, alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.MANAGE_ALLIANCE)) {
      logger.warn("Saving alliance text failed, unauthorized access: userId={} allianceId={}", tuple.user.getId(),
          allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    logger.info("Saving alliance text successful: userId={} allianceId={}", tuple.user.getId(), allianceId);

    assert kind != null;
    switch (kind) {
      case EXTERNAL:
        alliance.setExternalText(text);
        break;
      case INTERNAL:
        alliance.setInternalText(text);
        break;
      case APPLICATION:
        alliance.setApplicationText(text);
        break;
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void disband(long bodyId, long allianceId, String password) {
    if (!userServiceInternal.checkCurrentUserPassword(password)) {
      throw new WrongPasswordException();
    }

    UserAndAllianceAndMemberTuple tuple = getUserAndAllianceAndMember(allianceId);
    Alliance alliance = tuple.alliance;

    int privileges = getPrivileges(tuple.user, alliance, tuple.member);
    if (!hasPrivilege(privileges, AlliancePrivilege.DISBAND_ALLIANCE)) {
      logger.warn("Disbanding alliance failed, unauthorized access: userId={} allianceId={}", tuple.user.getId(),
          allianceId);
      throw new UnauthorizedAllianceAccessException();
    }

    // Get members for the update of cache.
    List<Long> memberIds = alliance.getMembers().stream()
        .map(m -> m.getUser().getId())
        .collect(Collectors.toList());

    logger.info("Disbanding alliance successful: userId={} allianceId={}", tuple.user.getId(), allianceId);
    allianceRepository.delete(alliance);

    // Update cache.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        for (Long id : memberIds) {
          userAllianceCache.removeUserAlliance(id);
        }
        allianceTagCache.removeTag(allianceId);
      }
    });
  }
}
