package com.github.retro_game.retro_game.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "galaxy")
public class GalaxySlot {
  @Column(name = "galaxy", nullable = false, insertable = false, updatable = false)
  private int galaxy;

  @Column(name = "system", nullable = false, insertable = false, updatable = false)
  private int system;

  @Column(name = "position", nullable = false, insertable = false, updatable = false)
  private int position;

  @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
  private long userId;

  @Column(name = "user_name", nullable = false, insertable = false, updatable = false)
  private String userName;

  // There must be at least one Id field, planetId should be unique.
  @Column(name = "planet_id", nullable = false, insertable = false, updatable = false)
  @Id
  private long planetId;

  @Column(name = "planet_name", nullable = false, insertable = false, updatable = false)
  private String planetName;

  @Column(name = "planet_type", nullable = false, insertable = false, updatable = false)
  private BodyType planetType;

  @Column(name = "planet_image", nullable = false, insertable = false, updatable = false)
  private int planetImage;

  @Column(name = "moon_id", insertable = false, updatable = false)
  private Long moonId;

  @Column(name = "moon_name", insertable = false, updatable = false)
  private String moonName;

  @Column(name = "moon_image", insertable = false, updatable = false)
  private Integer moonImage;

  @Column(name = "debris_metal", insertable = false, updatable = false)
  private Long debrisMetal;

  @Column(name = "debris_crystal", insertable = false, updatable = false)
  private Long debrisCrystal;

  public int getGalaxy() {
    return galaxy;
  }

  public int getSystem() {
    return system;
  }

  public int getPosition() {
    return position;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public long getPlanetId() {
    return planetId;
  }

  public String getPlanetName() {
    return planetName;
  }

  public BodyType getPlanetType() {
    return planetType;
  }

  public int getPlanetImage() {
    return planetImage;
  }

  public Long getMoonId() {
    return moonId;
  }

  public String getMoonName() {
    return moonName;
  }

  public Integer getMoonImage() {
    return moonImage;
  }

  public Long getDebrisMetal() {
    return debrisMetal;
  }

  public Long getDebrisCrystal() {
    return debrisCrystal;
  }
}
