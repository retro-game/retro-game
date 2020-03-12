package com.github.retro_game.retro_game.service.impl.item.technology;

import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.entity.TechnologyKind;
import com.github.retro_game.retro_game.service.impl.item.Item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class TechnologyItem extends Item {
  private static final Map<TechnologyKind, TechnologyItem> technologies =
      Collections.unmodifiableMap(new EnumMap<TechnologyKind, TechnologyItem>(TechnologyKind.class) {{
        put(TechnologyKind.ESPIONAGE_TECHNOLOGY, new EspionageTechnology());
        put(TechnologyKind.COMPUTER_TECHNOLOGY, new ComputerTechnology());
        put(TechnologyKind.WEAPONS_TECHNOLOGY, new WeaponsTechnology());
        put(TechnologyKind.SHIELDING_TECHNOLOGY, new ShieldingTechnology());
        put(TechnologyKind.ARMOR_TECHNOLOGY, new ArmorTechnology());
        put(TechnologyKind.ENERGY_TECHNOLOGY, new EnergyTechnology());
        put(TechnologyKind.HYPERSPACE_TECHNOLOGY, new HyperspaceTechnology());
        put(TechnologyKind.COMBUSTION_DRIVE, new CombustionDrive());
        put(TechnologyKind.IMPULSE_DRIVE, new ImpulseDrive());
        put(TechnologyKind.HYPERSPACE_DRIVE, new HyperspaceDrive());
        put(TechnologyKind.LASER_TECHNOLOGY, new LaserTechnology());
        put(TechnologyKind.ION_TECHNOLOGY, new IonTechnology());
        put(TechnologyKind.PLASMA_TECHNOLOGY, new PlasmaTechnology());
        put(TechnologyKind.INTERGALACTIC_RESEARCH_NETWORK, new IntergalacticResearchNetwork());
        put(TechnologyKind.ASTROPHYSICS, new Astrophysics());
        put(TechnologyKind.GRAVITON_TECHNOLOGY, new GravitonTechnology());
      }});

  public static Map<TechnologyKind, TechnologyItem> getAll() {
    return technologies;
  }

  public abstract Resources getBaseCost();

  public int getBaseRequiredEnergy() {
    return 0;
  }

  public double getCostFactor() {
    return 2.0;
  }
}
