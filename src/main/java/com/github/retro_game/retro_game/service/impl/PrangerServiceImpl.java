package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.model.entity.PrangerEntry;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.PrangerEntryRepository;
import com.github.retro_game.retro_game.service.dto.PrangerEntryDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("prangerService")
public class PrangerServiceImpl implements PrangerServiceInternal {
  private final PrangerEntryRepository prangerEntryRepository;

  public PrangerServiceImpl(PrangerEntryRepository prangerEntryRepository) {
    this.prangerEntryRepository = prangerEntryRepository;
  }

  @Override
  public List<PrangerEntryDto> get(long bodyId) {
    return prangerEntryRepository.findByOrderByAtDesc().stream()
        .map(e -> {
          User user = e.getUser();
          return new PrangerEntryDto(user.getId(), user.getName(), e.getAt(), e.getUntil(), e.getReason());
        })
        .collect(Collectors.toList());
  }

  @Override
  public void createEntry(User user, Date at, Date until, String reason, User admin) {
    PrangerEntry entry = new PrangerEntry();
    entry.setUser(user);
    entry.setAt(at);
    entry.setUntil(until);
    entry.setReason(reason);
    entry.setAdmin(admin);
    prangerEntryRepository.save(entry);
  }

  @Override
  public void deleteEntry(User user, Date until) {
    Optional<PrangerEntry> entryOptional = prangerEntryRepository.findByUserAndUntil(user, until);
    assert entryOptional.isPresent();
    prangerEntryRepository.delete(entryOptional.get());
  }
}
