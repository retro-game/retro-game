package com.github.retro_game.retro_game.controller.form;

import com.github.retro_game.retro_game.dto.CoordinatesKindDto;
import com.github.retro_game.retro_game.dto.MissionDto;
import com.github.retro_game.retro_game.dto.UnitKindDto;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class SendFleetForm {
  private long body;

  @NotNull
  @NotEmpty
  private Map<UnitKindDto, Integer> units;

  @NotNull
  private MissionDto mission;

  @Range(min = 0, max = 100)
  private Integer holdTime;

  @Range(min = 1, max = 5)
  private int galaxy;

  @Range(min = 1, max = 500)
  private int system;

  @Range(min = 1, max = 15)
  private int position;

  @NotNull
  private CoordinatesKindDto kind;

  @Range(min = 1, max = 10)
  private int factor;

  private Long party;

  @Min(0)
  private Long metal;

  @Min(0)
  private Long crystal;

  @Min(0)
  private Long deuterium;

  public long getBody() {
    return body;
  }

  public void setBody(long body) {
    this.body = body;
  }

  public Map<UnitKindDto, Integer> getUnits() {
    return units;
  }

  public void setUnits(Map<UnitKindDto, Integer> units) {
    this.units = units;
  }

  public MissionDto getMission() {
    return mission;
  }

  public void setMission(MissionDto mission) {
    this.mission = mission;
  }

  public Integer getHoldTime() {
    return holdTime;
  }

  public void setHoldTime(Integer holdTime) {
    this.holdTime = holdTime;
  }

  public int getGalaxy() {
    return galaxy;
  }

  public void setGalaxy(int galaxy) {
    this.galaxy = galaxy;
  }

  public int getSystem() {
    return system;
  }

  public void setSystem(int system) {
    this.system = system;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public CoordinatesKindDto getKind() {
    return kind;
  }

  public void setKind(CoordinatesKindDto kind) {
    this.kind = kind;
  }

  public int getFactor() {
    return factor;
  }

  public void setFactor(int factor) {
    this.factor = factor;
  }

  public Long getParty() {
    return party;
  }

  public void setParty(Long party) {
    this.party = party;
  }

  public Long getMetal() {
    return metal;
  }

  public void setMetal(Long metal) {
    this.metal = metal;
  }

  public Long getCrystal() {
    return crystal;
  }

  public void setCrystal(Long crystal) {
    this.crystal = crystal;
  }

  public Long getDeuterium() {
    return deuterium;
  }

  public void setDeuterium(Long deuterium) {
    this.deuterium = deuterium;
  }
}
