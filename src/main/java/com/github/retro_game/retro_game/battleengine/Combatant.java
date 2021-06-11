package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.UnitKind;

import java.util.EnumMap;

public class Combatant {
  private final long userId;
  private final Coordinates coordinates;
  private final int weaponsTechnology;
  private final int shieldingTechnology;
  private final int armorTechnology;
  private final EnumMap<UnitKind, Long> unitGroups;

  public Combatant(long userId, Coordinates coordinates, int weaponsTechnology, int shieldingTechnology,
                   int armorTechnology, EnumMap<UnitKind, Long> unitGroups) {
    assert weaponsTechnology >= 0;
    assert shieldingTechnology >= 0;
    assert armorTechnology >= 0;
    assert unitGroups.values().stream().allMatch(count -> count >= 0L);

    this.userId = userId;
    this.coordinates = coordinates;
    this.weaponsTechnology = weaponsTechnology;
    this.shieldingTechnology = shieldingTechnology;
    this.armorTechnology = armorTechnology;
    this.unitGroups = unitGroups;
  }

  public long getUserId() {
    return userId;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public int getWeaponsTechnology() {
    return weaponsTechnology;
  }

  public int getShieldingTechnology() {
    return shieldingTechnology;
  }

  public int getArmorTechnology() {
    return armorTechnology;
  }

  public EnumMap<UnitKind, Long> getUnitGroups() {
    return unitGroups;
  }
}
