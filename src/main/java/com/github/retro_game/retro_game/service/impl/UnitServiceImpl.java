package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.model.unit.UnitItem;
import org.springframework.stereotype.Service;

@Service
class UnitServiceImpl implements UnitService {
  @Override
  public int getSpeed(UnitKind kind, User user) {
    UnitItem item = UnitItem.getAll().get(kind);

    int speed = item.getBaseSpeed(user);
    if (speed == 0) {
      return 0;
    }

    TechnologyKind drive = item.getDrive(user);
    if (drive == null) {
      return 0;
    }
    int level = user.getTechnologyLevel(drive);

    assert speed % 10 == 0;
    switch (drive) {
      case COMBUSTION_DRIVE:
        return speed + speed / 10 * level;
      case IMPULSE_DRIVE:
        return speed + speed / 10 * 2 * level;
      case HYPERSPACE_DRIVE:
        return speed + speed / 10 * 3 * level;
    }
    return 0;
  }

  @Override
  public double getWeapons(UnitKind kind, User user) {
    return UnitItem.getAll().get(kind).getBaseWeapons() *
        (1.0 + 0.1 * user.getTechnologyLevel(TechnologyKind.WEAPONS_TECHNOLOGY));
  }

  @Override
  public double getShield(UnitKind kind, User user) {
    return UnitItem.getAll().get(kind).getBaseShield() *
        (1.0 + 0.1 * user.getTechnologyLevel(TechnologyKind.SHIELDING_TECHNOLOGY));
  }

  @Override
  public double getArmor(UnitKind kind, User user) {
    return UnitItem.getAll().get(kind).getBaseArmor() *
        (1.0 + 0.1 * user.getTechnologyLevel(TechnologyKind.ARMOR_TECHNOLOGY));
  }
}
