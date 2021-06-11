package com.github.retro_game.retro_game.battleengine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
@ConditionalOnProperty(value = "retro-game.battle-engine", havingValue = "native")
public final class NativeBattleEngine implements BattleEngine {
  static {
    System.loadLibrary("BattleEngine");
  }

  public NativeBattleEngine() {
    var unitsAttributes = UnitAttributes.makeUnitsAttributes();
    var success = init(unitsAttributes);
    Assert.isTrue(success, "Failed to init battle engine");
  }

  private native boolean init(UnitAttributes[] unitsAttributes);

  @Override
  public native BattleOutcome fight(List<Combatant> attackers, List<Combatant> defenders, int seed);
}
