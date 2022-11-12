package com.github.retro_game.retro_game.model.unit;

import com.github.retro_game.retro_game.entity.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BattleCruiser extends UnitItem {

    private static final Map<BuildingKind, Integer> buildingsRequirements =
            Collections.singletonMap(BuildingKind.SHIPYARD, 8);

    private static final Map<TechnologyKind, Integer> technologiesRequirements =
            Collections.unmodifiableMap(new EnumMap<>(TechnologyKind.class) {{
                put(TechnologyKind.HYPERSPACE_TECHNOLOGY, 5);
                put(TechnologyKind.HYPERSPACE_DRIVE, 5);
                put(TechnologyKind.LASER_TECHNOLOGY, 12);
            }});

    private static final Map<UnitKind, Integer> rapidFireAgainst =
            Collections.unmodifiableMap(new EnumMap<>(UnitKind.class) {{
                put(UnitKind.ESPIONAGE_PROBE, 5);
                put(UnitKind.SOLAR_SATELLITE, 5);
                put(UnitKind.SMALL_CARGO, 3);
                put(UnitKind.LARGE_CARGO, 3);
                put(UnitKind.HEAVY_FIGHTER, 4);
                put(UnitKind.CRUISER, 4);
                put(UnitKind.BATTLESHIP, 7);
            }});

    @Override
    public Map<BuildingKind, Integer> getBuildingsRequirements() {
        return buildingsRequirements;
    }

    @Override
    public Map<TechnologyKind, Integer> getTechnologiesRequirements() {
        return technologiesRequirements;
    }

    @Override
    public Resources getCost() {
        return new Resources(30000, 40000, 15000);
    }

    @Override
    public int getCapacity() {
        return 750;
    }

    @Override
    public int getConsumption(User user) {
        return 250;
    }

    @Override
    public TechnologyKind getDrive(User user) {
        return TechnologyKind.HYPERSPACE_DRIVE;
    }

    @Override
    public int getBaseSpeed(User user) {
        return 10000;
    }

    @Override
    public double getBaseWeapons() {
        return 700;
    }

    @Override
    public double getBaseShield() {
        return 400;
    }

    @Override
    public Map<UnitKind, Integer> getRapidFireAgainst() {
        return rapidFireAgainst;
    }
}
