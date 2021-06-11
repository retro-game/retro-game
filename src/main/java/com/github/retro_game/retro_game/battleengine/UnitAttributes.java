package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.unit.UnitItem;

import java.util.Map;

final class UnitAttributes {
  final float weapons;
  final float shield;
  final float armor;
  final int[] rapidFire;

  private UnitAttributes(float weapons, float shield, float armor, int[] rapidFire) {
    this.weapons = weapons;
    this.shield = shield;
    this.armor = armor;
    this.rapidFire = rapidFire;
  }

  static int[] makeRapidFire(Map<UnitKind, Integer> rapidFireAgainst) {
    var rapidFire = new int[UnitKind.values().length];
    for (var entry : rapidFireAgainst.entrySet()) {
      var kind = entry.getKey();
      var n = entry.getValue();
      rapidFire[kind.ordinal()] = n;
    }
    return rapidFire;
  }

  static UnitAttributes[] makeUnitsAttributes() {
    var attrs = new UnitAttributes[UnitKind.values().length];
    for (var entry : UnitItem.getAll().entrySet()) {
      var kind = entry.getKey();
      var item = entry.getValue();
      var weapons = (float) item.getBaseWeapons();
      var shield = (float) item.getBaseShield();
      var armor = (float) item.getBaseArmor();
      var rapidFire = makeRapidFire(item.getRapidFireAgainst());
      attrs[kind.ordinal()] = new UnitAttributes(weapons, shield, armor, rapidFire);
    }
    return attrs;
  }
}
