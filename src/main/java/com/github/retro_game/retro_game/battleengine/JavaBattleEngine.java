package com.github.retro_game.retro_game.battleengine;

import com.github.retro_game.retro_game.entity.UnitKind;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

@Component
@ConditionalOnProperty(value = "retro-game.battle-engine", havingValue = "java")
public final class JavaBattleEngine implements BattleEngine {
  // Lehmer RNG
  // Using this simple RNG improves the performance of the battle engine by a wide margin.
  // Keep in sync with the RNG in the native battle engine.
  // TODO: We need a RNG that returns 64-bit integers.
  private static final class Random {
    private static final int MODULUS = 2_147_483_647;
    private static final int MAX = MODULUS - 1;
    private static final int MULTIPLIER = 48_271;

    private static int next(int r) {
      return (int) ((long) r * (long) MULTIPLIER % (long) MODULUS);
    }
  }

  private static final int MAX_ROUNDS = 6;
  private static final UnitAttributes[] unitsAttributes = UnitAttributes.makeUnitsAttributes();

  private static final class Units {
    private int numAlive;
    private final float[] shields;
    private final float[] hulls;
    private final byte[] kinds;
    private final byte[] ids;

    private Units(int numAlive, float[] shields, float[] hulls, byte[] kinds, byte[] ids) {
      this.numAlive = numAlive;
      this.shields = shields;
      this.hulls = hulls;
      this.kinds = kinds;
      this.ids = ids;
    }
  }

  private static final class Stats {
    private final int[] numRemainingUnits;
    private final long[] timesFired;
    private final long[] timesWasShot;
    private final float[] shieldDamageDealt;
    private final float[] hullDamageDealt;
    private final float[] shieldDamageTaken;
    private final float[] hullDamageTaken;

    private Stats(int[] numRemainingUnits, long[] timesFired, long[] timesWasShot, float[] shieldDamageDealt,
                  float[] hullDamageDealt, float[] shieldDamageTaken, float[] hullDamageTaken) {
      this.numRemainingUnits = numRemainingUnits;
      this.timesFired = timesFired;
      this.timesWasShot = timesWasShot;
      this.shieldDamageDealt = shieldDamageDealt;
      this.hullDamageDealt = hullDamageDealt;
      this.shieldDamageTaken = shieldDamageTaken;
      this.hullDamageTaken = hullDamageTaken;
    }
  }

  private static final class Party {
    private final Units units;
    private final Stats stats;

    private Party(Units units, Stats stats) {
      this.units = units;
      this.stats = stats;
    }
  }

  private static Units makeUnits(Combatant[] combatants) {
    assert UnitKind.values().length <= Byte.MAX_VALUE;
    assert combatants.length <= Byte.MAX_VALUE;

    var totalUnits = Arrays.stream(combatants)
        .mapToLong(c -> c.getUnitGroups().values().stream().mapToLong(Long::longValue).sum())
        .sum();
    if (totalUnits > Integer.MAX_VALUE) {
      // We cannot make bigger arrays in Java.
      throw new IllegalArgumentException("Too many units");
    }

    var shields = new float[(int) totalUnits];
    var hulls = new float[(int) totalUnits];
    var kinds = new byte[(int) totalUnits];
    var ids = new byte[(int) totalUnits];

    var n = 0;
    for (var i = 0; i < combatants.length; i++) {
      var combatant = combatants[i];
      for (var item : combatant.getUnitGroups().entrySet()) {
        var kind = item.getKey().ordinal();
        var count = item.getValue();
        var maxHull = 0.1f * unitsAttributes[kind].armor * (1.0f + 0.1f * combatant.getArmorTechnology());
        for (var j = 0; j < count; j++) {
          hulls[n] = maxHull;
          kinds[n] = (byte) kind;
          ids[n] = (byte) i;
          n++;
        }
      }
    }

    return new Units((int) totalUnits, shields, hulls, kinds, ids);
  }

  private static Stats makeStats(Combatant[] combatants) {
    assert combatants.length <= Integer.MAX_VALUE / (MAX_ROUNDS * UnitKind.values().length);
    var size = combatants.length * MAX_ROUNDS * UnitKind.values().length;
    var numRemainingUnits = new int[size];
    var timesFired = new long[size];
    var timesWasShot = new long[size];
    var shieldDamageDealt = new float[size];
    var hullDamageDealt = new float[size];
    var shieldDamageTaken = new float[size];
    var hullDamageTaken = new float[size];
    return new Stats(numRemainingUnits, timesFired, timesWasShot, shieldDamageDealt, hullDamageDealt, shieldDamageTaken,
        hullDamageTaken);
  }

  private static Party makeParty(Combatant[] combatants) {
    var units = makeUnits(combatants);
    var stats = makeStats(combatants);
    return new Party(units, stats);
  }

  private static void restoreShields(Combatant[] combatants, Party party) {
    var units = party.units;
    for (var i = 0; i < units.numAlive; i++) {
      var kind = units.kinds[i];
      var id = units.ids[i];
      var combatant = combatants[id];
      var shield = unitsAttributes[kind].shield * (1.0f + 0.1f * combatant.getShieldingTechnology());
      units.shields[i] = shield;
    }
  }

  private static int fire(Combatant[] attackers, Combatant[] defenders, Party attackersParty, Party defendersParty,
                          int round, int random) {
    var r = random;

    final var attackersUnits = attackersParty.units;
    final var defendersUnits = defendersParty.units;

    final var attackersStats = attackersParty.stats;
    final var defendersStats = defendersParty.stats;

    final var numShooters = attackersUnits.numAlive;
    final var numTargets = defendersUnits.numAlive;

    // Each shooter fires at one or more random targets.
    for (var i = 0; i < numShooters; i++) {
      final var shooterKind = attackersUnits.kinds[i];
      final var attackerId = attackersUnits.ids[i];
      final var attacker = attackers[attackerId];
      final var shooterStatsIdx = attackerId * MAX_ROUNDS * UnitKind.values().length +
          round * UnitKind.values().length + shooterKind;

      final var damage = unitsAttributes[shooterKind].weapons * (1.0f + 0.1f * attacker.getWeaponsTechnology());

      while (true) {
        // Pick a random target.
        r = Random.next(r);
        final var targetIdx = r % numTargets;

        var shield = defendersUnits.shields[targetIdx];
        var hull = defendersUnits.hulls[targetIdx];
        final var targetKind = defendersUnits.kinds[targetIdx];
        final var defenderId = defendersUnits.ids[targetIdx];
        final var defender = defenders[defenderId];
        final var targetStatsIdx = defenderId * MAX_ROUNDS * UnitKind.values().length +
            round * UnitKind.values().length + targetKind;

        attackersStats.timesFired[shooterStatsIdx]++;
        defendersStats.timesWasShot[targetStatsIdx]++;

        // Is the target alive?
        if (hull != 0.0f) {
          var hullDamage = damage - shield;

          // Does the shooter break through the shield at all?
          if (hullDamage < 0.0f) {
            // All damage absorbed by the shield. Calculate the shield damage including the bouncing effect.
            var maxShield = unitsAttributes[targetKind].shield * (1.0f + 0.1f * defender.getShieldingTechnology());
            var shieldDamage = 0.01f * (float) Math.floor(100.0f * damage / maxShield) * maxShield;
            shield -= shieldDamage;

            attackersStats.shieldDamageDealt[shooterStatsIdx] += shieldDamage;
            defendersStats.shieldDamageTaken[targetStatsIdx] += shieldDamage;
          } else {
            // We break through the shield.
            var shieldDamage = shield;
            shield = 0.0f;

            if (hullDamage > hull) {
              hullDamage = hull;
            }
            hull -= hullDamage;

            attackersStats.shieldDamageDealt[shooterStatsIdx] += shieldDamage;
            attackersStats.hullDamageDealt[shooterStatsIdx] += hullDamage;
            defendersStats.shieldDamageTaken[targetStatsIdx] += shieldDamage;
            defendersStats.hullDamageTaken[targetStatsIdx] += hullDamage;
          }

          if (hull != 0.0f) {
            // If the target's hull is less than 70%, the target might explode.
            var maxHull = 0.1f * unitsAttributes[targetKind].armor * (1.0f + 0.1f * defender.getArmorTechnology());
            if (hull < 0.7f * maxHull) {
              r = Random.next(r);
              if (hull < (1.0f / Random.MAX * r * maxHull)) {
                // The target explodes, we mark it as dead.
                hull = 0.0f;
              }
            }
          }

          defendersUnits.shields[targetIdx] = shield;
          defendersUnits.hulls[targetIdx] = hull;
        }

        var rapidFire = unitsAttributes[shooterKind].rapidFire[targetKind];
        if (rapidFire == 0) {
          break;
        }

        r = Random.next(r);
        if (r % rapidFire == 0) {
          break;
        }
      }
    }

    return r;
  }

  // Remove (place at the end) the dead units, and update the stats.
  private static void updateUnits(Party party, int round) {
    var units = party.units;
    var stats = party.stats;

    var n = 0;
    for (var i = 0; i < units.numAlive; i++) {
      if (units.hulls[i] != 0.0f) {
        units.shields[n] = units.shields[i];
        units.hulls[n] = units.hulls[i];
        units.kinds[n] = units.kinds[i];
        units.ids[n] = units.ids[i];
        n++;

        int idx = units.ids[i] * MAX_ROUNDS * UnitKind.values().length +
            round * UnitKind.values().length +
            units.kinds[i];
        stats.numRemainingUnits[idx]++;
      }
    }

    units.numAlive = n;
  }

  private static List<CombatantOutcome> makeOutcomes(Combatant[] combatants, Party party, int numRounds) {
    var stats = party.stats;
    var outcomes = new ArrayList<CombatantOutcome>(combatants.length);
    for (var i = 0; i < combatants.length; i++) {
      var combatantStats = new ArrayList<EnumMap<UnitKind, UnitGroupStats>>(numRounds);
      for (var round = 0; round < numRounds; round++) {
        var roundStats = new EnumMap<UnitKind, UnitGroupStats>(UnitKind.class);
        for (var kind : UnitKind.values()) {
          int idx = i * MAX_ROUNDS * UnitKind.values().length + round * UnitKind.values().length + kind.ordinal();
          roundStats.put(kind, new UnitGroupStats(
              stats.numRemainingUnits[idx],
              stats.timesFired[idx],
              stats.timesWasShot[idx],
              stats.shieldDamageDealt[idx],
              stats.hullDamageDealt[idx],
              stats.shieldDamageTaken[idx],
              stats.hullDamageTaken[idx]
          ));
        }
        combatantStats.add(roundStats);
      }
      outcomes.add(new CombatantOutcome(combatantStats));
    }
    return outcomes;
  }

  @Override
  public BattleOutcome fight(List<Combatant> attackersList, List<Combatant> defendersList, int seed) {
    // Our RNG needs a positive seed.
    // Keep the calculation of the seed in sync with the native battle engine.
    if (seed < 0)
      seed = -seed;
    if (seed < 0 || seed == 0)
      seed = 1;
    var r = seed;

    var attackers = attackersList.toArray(new Combatant[0]);
    var defenders = defendersList.toArray(new Combatant[0]);

    var attackersParty = makeParty(attackers);
    var defendersParty = makeParty(defenders);

    var round = 0;
    while (round < MAX_ROUNDS && attackersParty.units.numAlive > 0 && defendersParty.units.numAlive > 0) {
      restoreShields(attackers, attackersParty);
      restoreShields(defenders, defendersParty);

      r = fire(attackers, defenders, attackersParty, defendersParty, round, r);
      r = fire(defenders, attackers, defendersParty, attackersParty, round, r);

      updateUnits(attackersParty, round);
      updateUnits(defendersParty, round);

      round++;
    }

    var numRounds = round;
    var attackersOutcomes = makeOutcomes(attackers, attackersParty, numRounds);
    var defendersOutcomes = makeOutcomes(defenders, defendersParty, numRounds);
    return new BattleOutcome(numRounds, attackersOutcomes, defendersOutcomes);
  }
}
