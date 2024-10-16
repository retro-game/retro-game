package com.github.retro_game.retro_game.differential;

import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.battleengine.CombatantOutcome;
import com.github.retro_game.retro_game.battleengine.JavaBattleEngineStrategy;
import com.github.retro_game.retro_game.battleengine.NativeBattleEngineStrategy;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class BattleEngineDifferentialTest {
  private static final long RANDOM_SEED = 42L;
  private static final int NUM_BATTLES = 1000;
  private final JavaBattleEngineStrategy javaBattleEngine = new JavaBattleEngineStrategy();
  private final NativeBattleEngineStrategy nativeBattleEngine = new NativeBattleEngineStrategy();

  private static Combatant generateCombatant(Random random) {
    var weaponsTechnology = random.nextInt(30);
    var shieldingTechnology = random.nextInt(30);
    var armorTechnology = random.nextInt(30);
    var unitGroups = new EnumMap<UnitKind, Long>(UnitKind.class);
    var numKinds = random.nextInt(UnitKind.values().length + 1);
    for (var i = 0; i < numKinds; i++) {
      var k = random.nextInt(UnitKind.values().length);
      var kind = UnitKind.values()[k];
      var n = (long) random.nextInt(1000);
      unitGroups.put(kind, n);
    }
    return new Combatant(1, new Coordinates(1, 1, 1, CoordinatesKind.PLANET), weaponsTechnology, shieldingTechnology,
        armorTechnology, unitGroups);
  }

  private static List<Combatant> generateCombatants(Random random) {
    var numCombatants = random.nextInt(10);
    var combatants = new ArrayList<Combatant>(numCombatants);
    for (var i = 0; i < numCombatants; i++)
      combatants.add(generateCombatant(random));
    return combatants;
  }

  private void assertOutcomesEqual(List<CombatantOutcome> lhs, List<CombatantOutcome> rhs) {
    Assertions.assertEquals(lhs.size(), rhs.size());
    for (var i = 0; i < lhs.size(); i++) {
      var a = lhs.get(i);
      var b = rhs.get(i);
      Assertions.assertEquals(a.unitGroupsStats(), b.unitGroupsStats());
    }
  }

  @Test
  public void test() {
    long javaTime = 0;
    long nativeTime = 0;

    var random = new Random(RANDOM_SEED);

    for (var i = 0; i < NUM_BATTLES; i++) {
      var seed = random.nextInt();
      var attackers = generateCombatants(random);
      var defenders = generateCombatants(random);

      long t1 = System.nanoTime();
      var javaOutcome = javaBattleEngine.fight(attackers, defenders, seed);
      long t2 = System.nanoTime();
      var nativeOutcome = nativeBattleEngine.fight(attackers, defenders, seed);
      long t3 = System.nanoTime();

      javaTime += t2 - t1;
      nativeTime += t3 - t2;

      assertOutcomesEqual(javaOutcome.attackersOutcomes(), nativeOutcome.attackersOutcomes());
      assertOutcomesEqual(javaOutcome.defendersOutcomes(), nativeOutcome.defendersOutcomes());
    }

    System.out.printf("Battle Engine Perf: java=%,dns native=%,dns\n", javaTime, nativeTime);
  }
}
