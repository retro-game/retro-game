package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.battleengine.BattleOutcome;
import com.github.retro_game.retro_game.battleengine.Combatant;
import com.github.retro_game.retro_game.battleengine.CombatantOutcome;
import com.github.retro_game.retro_game.battleengine.UnitGroupStats;
import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.model.unit.UnitItem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class CombatReportSerialization {
  public static byte[] serialize(List<Combatant> attackers, List<Combatant> defenders, BattleOutcome battleOutcome) {
    try {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      var stream = new DataOutputStream(byteArrayOutputStream);
      storeCombatants(stream, attackers);
      storeCombatants(stream, defenders);
      storeRounds(stream, attackers, defenders, battleOutcome);
      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public record Data(ArrayList<CombatReportCombatantDto> attackers, ArrayList<CombatReportCombatantDto> defenders,
                     ArrayList<CombatReportRoundDto> rounds) {
  }

  public static Data deserialize(byte[] data) {
    try {
      var stream = new DataInputStream(new ByteArrayInputStream(data));
      var attackers = loadCombatants(stream);
      var defenders = loadCombatants(stream);
      var rounds = loadRounds(stream);
      return new Data(attackers, defenders, rounds);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void storeCombatants(DataOutputStream stream, List<Combatant> combatants) throws IOException {
    var numCombatants = combatants.size();
    assert numCombatants <= 255;
    stream.writeByte(numCombatants);
    for (var combatant : combatants) {
      storeCombatant(stream, combatant);
    }
  }

  private static ArrayList<CombatReportCombatantDto> loadCombatants(DataInputStream stream) throws IOException {
    var numCombatants = stream.readUnsignedByte();
    var combatants = new ArrayList<CombatReportCombatantDto>(numCombatants);
    for (var i = 0; i < numCombatants; i++) {
      var combatant = loadCombatant(stream);
      combatants.add(combatant);
    }
    return combatants;
  }

  private static void storeCombatant(DataOutputStream stream, Combatant combatant) throws IOException {
    stream.writeLong(combatant.getUserId());

    var coords = combatant.getCoordinates();
    stream.writeInt(coords.getGalaxy());
    stream.writeInt(coords.getSystem());
    stream.writeInt(coords.getPosition());
    stream.writeByte(coords.getKind().ordinal());

    stream.writeByte(combatant.getWeaponsTechnology());
    stream.writeByte(combatant.getShieldingTechnology());
    stream.writeByte(combatant.getArmorTechnology());

    var unitGroups = combatant.getUnitGroups();
    assert UnitKind.values().length <= Byte.MAX_VALUE;
    var numGroups = (int) unitGroups.values().stream().filter(i -> i != 0).count();
    stream.writeByte(numGroups);
    for (var entry : unitGroups.entrySet()) {
      var kind = entry.getKey();
      var count = entry.getValue();
      assert count >= 0;
      if (count > 0) {
        stream.writeByte(kind.ordinal());
        stream.writeLong(count);
      }
    }
  }

  private static CombatReportCombatantDto loadCombatant(DataInputStream stream) throws IOException {
    var userId = stream.readLong();
    assert userId > 0;

    var g = stream.readInt();
    var s = stream.readInt();
    var p = stream.readInt();
    var k = CoordinatesKind.values()[stream.readUnsignedByte()];
    var coords = Converter.convert(new Coordinates(g, s, p, k));

    var weaponsTechnology = stream.readUnsignedByte();
    var shieldingTechnology = stream.readUnsignedByte();
    var armorTechnology = stream.readUnsignedByte();

    var unitGroups = new EnumMap<UnitKindDto, CombatReportUnitGroupDto>(UnitKindDto.class);
    var numGroups = stream.readUnsignedByte();
    for (var i = 0; i < numGroups; i++) {
      var kind = UnitKind.values()[stream.readUnsignedByte()];
      var count = stream.readLong();
      assert count > 0;

      var item = UnitItem.get(kind);
      var weapons = (1.0 + 0.1 * weaponsTechnology) * item.getBaseWeapons();
      var shields = (1.0 + 0.1 * shieldingTechnology) * item.getBaseShield();
      var armor = (1.0 + 0.1 * armorTechnology) * item.getBaseArmor();

      unitGroups.put(Converter.convert(kind), new CombatReportUnitGroupDto(count, weapons, shields, armor));
    }

    return new CombatReportCombatantDto(userId, coords, weaponsTechnology, shieldingTechnology, armorTechnology,
        unitGroups);
  }

  private static void storeRounds(DataOutputStream stream, List<Combatant> attackers, List<Combatant> defenders,
                                  BattleOutcome battleOutcome) throws IOException {
    var numRounds = battleOutcome.getNumRounds();
    assert numRounds >= 0 && numRounds <= 255;
    stream.writeByte(numRounds);

    var attackersOutcomes = battleOutcome.getAttackersOutcomes();
    var defendersOutcomes = battleOutcome.getDefendersOutcomes();
    for (var round = 0; round < numRounds; round++) {
      storeRoundCombatants(stream, attackers, attackersOutcomes, round);
      storeRoundCombatants(stream, defenders, defendersOutcomes, round);
    }
  }

  private static ArrayList<CombatReportRoundDto> loadRounds(DataInputStream stream) throws IOException {
    var numRounds = stream.readUnsignedByte();
    var rounds = new ArrayList<CombatReportRoundDto>(numRounds);
    for (var i = 0; i < numRounds; i++) {
      var attackers = loadRoundCombatants(stream);
      var defenders = loadRoundCombatants(stream);
      rounds.add(new CombatReportRoundDto(attackers, defenders));
    }
    return rounds;
  }

  private static void storeRoundCombatants(DataOutputStream stream, List<Combatant> combatants,
                                           List<CombatantOutcome> outcomes, int round) throws IOException {
    int[] numActiveGroups = outcomes.stream().mapToInt(
        outcome -> (int) outcome.getNthRoundUnitGroupsStats(round).values().stream().filter(s -> s.getTimesFired() > 0)
            .count()).toArray();

    var numActiveCombatants = (int) Arrays.stream(numActiveGroups).filter(n -> n != 0).count();
    assert numActiveCombatants <= 255;
    stream.writeByte(numActiveCombatants);

    for (var i = 0; i < outcomes.size(); i++) {
      var numActive = numActiveGroups[i];
      if (numActive > 0) {
        var userId = combatants.get(i).getUserId();
        var unitGroupsStats = outcomes.get(i).getNthRoundUnitGroupsStats(round);
        storeRoundCombatant(stream, userId, unitGroupsStats, numActive);
      }
    }
  }

  private static ArrayList<CombatReportRoundCombatantDto> loadRoundCombatants(DataInputStream stream)
      throws IOException {
    var numActiveCombatants = stream.readUnsignedByte();
    var combatants = new ArrayList<CombatReportRoundCombatantDto>(numActiveCombatants);
    for (var i = 0; i < numActiveCombatants; i++) {
      var combatant = loadRoundCombatant(stream);
      combatants.add(combatant);
    }
    return combatants;
  }

  private static void storeRoundCombatant(DataOutputStream stream, long userId,
                                          EnumMap<UnitKind, UnitGroupStats> unitGroupsStats, int numActiveGroups)
      throws IOException {
    stream.writeLong(userId);
    storeRoundUnitGroups(stream, unitGroupsStats, numActiveGroups);
  }

  private static CombatReportRoundCombatantDto loadRoundCombatant(DataInputStream stream) throws IOException {
    var userId = stream.readLong();
    var unitGroups = loadRoundUnitGroups(stream);
    return new CombatReportRoundCombatantDto(userId, unitGroups);
  }

  private static void storeRoundUnitGroups(DataOutputStream stream, EnumMap<UnitKind, UnitGroupStats> unitGroupsStats,
                                           int numActiveGroups) throws IOException {
    assert numActiveGroups <= 255;
    stream.writeByte(numActiveGroups);
    for (var entry : unitGroupsStats.entrySet()) {
      var kind = entry.getKey();
      var stats = entry.getValue();
      var active = stats.getTimesFired() > 0;
      if (active) {
        stream.writeByte(kind.ordinal());
        stream.writeLong(stats.getTimesFired());
        stream.writeLong(stats.getTimesWasShot());
        stream.writeLong((long) stats.getShieldDamageDealt());
        stream.writeLong((long) stats.getHullDamageDealt());
        stream.writeLong((long) stats.getShieldDamageTaken());
        stream.writeLong((long) stats.getHullDamageTaken());
        stream.writeLong(stats.getNumRemainingUnits());
      }
    }
  }

  private static EnumMap<UnitKindDto, CombatReportRoundUnitGroupDto> loadRoundUnitGroups(DataInputStream stream)
      throws IOException {
    var numActiveGroups = stream.readUnsignedByte();
    var groups = new EnumMap<UnitKindDto, CombatReportRoundUnitGroupDto>(UnitKindDto.class);
    for (var i = 0; i < numActiveGroups; i++) {
      var kind = UnitKind.values()[stream.readUnsignedByte()];
      var timesFired = stream.readLong();
      var timesWasShot = stream.readLong();
      var shieldDamageDealt = stream.readLong();
      var hullDamageDealt = stream.readLong();
      var shieldDamageTaken = stream.readLong();
      var hullDamageTaken = stream.readLong();
      var numRemainingUnits = stream.readLong();
      var group = new CombatReportRoundUnitGroupDto(numRemainingUnits, timesFired, timesWasShot, shieldDamageDealt,
          hullDamageDealt, shieldDamageTaken, hullDamageTaken);
      groups.put(Converter.convert(kind), group);
    }
    return groups;
  }
}
