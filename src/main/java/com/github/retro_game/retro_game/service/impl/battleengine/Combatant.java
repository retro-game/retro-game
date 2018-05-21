package com.github.retro_game.retro_game.service.impl.battleengine;

import com.github.retro_game.retro_game.model.entity.UnitKind;

import java.util.Map;

public class Combatant {
  private final int userId;
  private final int weaponsTechnology;
  private final int shieldingTechnology;
  private final int armorTechnology;
  private final int[] unitGroups;

  public Combatant(int userId, int weaponsTechnology, int shieldingTechnology, int armorTechnology,
                   Map<UnitKind, Integer> unitGroups) {
    this.userId = userId;
    this.weaponsTechnology = weaponsTechnology;
    this.shieldingTechnology = shieldingTechnology;
    this.armorTechnology = armorTechnology;
    this.unitGroups = new int[UnitKind.values().length];
    for (Map.Entry<UnitKind, Integer> entry : unitGroups.entrySet()) {
      this.unitGroups[entry.getKey().ordinal()] = entry.getValue();
    }
  }

  public int getUserId() {
    return userId;
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

  public int[] getUnitGroups() {
    return unitGroups;
  }
}