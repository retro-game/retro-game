package com.github.retro_game.retro_game.service.impl.battleengine;

import com.github.retro_game.retro_game.model.entity.UnitKind;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
public class BattleEngine {
  static {
    System.loadLibrary("BattleEngine");
  }

  public BattleEngine() {
    Map<UnitKind, UnitItem> units = UnitItem.getAll();
    final UnitCharacteristics[] unitsCharacteristics = new UnitCharacteristics[units.size()];
    for (Map.Entry<UnitKind, UnitItem> entry : units.entrySet()) {
      UnitItem unit = entry.getValue();
      float weapons = (float) unit.getBaseWeapons();
      float shield = (float) unit.getBaseShield();
      float armor = (float) unit.getBaseArmor();
      int[] rapidFire = new int[units.size()];
      for (Map.Entry<UnitKind, Integer> rapidFireEntry : unit.getRapidFireAgainst().entrySet()) {
        rapidFire[rapidFireEntry.getKey().ordinal()] = rapidFireEntry.getValue();
      }
      unitsCharacteristics[entry.getKey().ordinal()] = new UnitCharacteristics(weapons, shield, armor, rapidFire);
    }

    boolean success = init(unitsCharacteristics);
    Assert.isTrue(success, "Failed to init battle engine");
  }

  private native boolean init(final UnitCharacteristics[] unitCharacteristics);

  public native BattleOutcome fight(Combatant[] attackers, Combatant[] defenders, int seed);
}
