package com.github.retro_game.retro_game.unit;

import com.github.retro_game.retro_game.battleengine.BattleEngine;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.EnumMap;

@SpringBootTest
public class BattleEngineTests {
  @Autowired
  private BattleEngine battleEngine;

  @Test
  public void testSeed0() {
    var attackers = Collections.singletonList(new Combatant(
        1,
        new Coordinates(1, 1, 1, CoordinatesKind.PLANET),
        10, 10, 10,
        new EnumMap<>(UnitKind.class) {{
          put(UnitKind.LITTLE_FIGHTER, 10L);
          put(UnitKind.CRUISER, 10L);
        }}
    ));
    var defenders = Collections.singletonList(new Combatant(
        2,
        new Coordinates(1, 1, 2, CoordinatesKind.PLANET),
        10, 10, 10,
        new EnumMap<>(UnitKind.class) {{
          put(UnitKind.BATTLESHIP, 10L);
        }}
    ));
    var outcome0 = battleEngine.fight(attackers, defenders, 0);
    var outcome1 = battleEngine.fight(attackers, defenders, 1);
    Assertions.assertEquals(
        outcome0.getAttackersOutcomes().get(0).getUnitGroupsStats(),
        outcome1.getAttackersOutcomes().get(0).getUnitGroupsStats()
    );
    Assertions.assertEquals(
        outcome0.getDefendersOutcomes().get(0).getUnitGroupsStats(),
        outcome1.getDefendersOutcomes().get(0).getUnitGroupsStats()
    );
  }
}
