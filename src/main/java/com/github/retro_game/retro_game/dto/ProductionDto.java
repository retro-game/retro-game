package com.github.retro_game.retro_game.dto;

public record ProductionDto(double efficiency, int metalBaseProduction, int crystalBaseProduction,
                            int deuteriumBaseProduction, int metalMineProduction, int metalMineCurrentEnergyUsage,
                            int metalMineMaxEnergyUsage, int crystalMineProduction, int crystalMineCurrentEnergyUsage,
                            int crystalMineMaxEnergyUsage, int deuteriumSynthesizerProduction,
                            int deuteriumSynthesizerCurrentEnergyUsage, int deuteriumSynthesizerMaxEnergyUsage,
                            int solarPlantEnergyProduction, int fusionReactorDeuteriumUsage,
                            int fusionReactorEnergyProduction, int singleSolarSatelliteEnergyProduction,
                            int solarSatellitesEnergyProduction, int metalProduction, int crystalProduction,
                            int deuteriumProduction, int totalEnergy, int usedEnergy, int availableEnergy) {
}
