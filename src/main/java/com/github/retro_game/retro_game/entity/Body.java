package com.github.retro_game.retro_game.entity;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "bodies")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
public class Body implements Serializable {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Embedded
  private Coordinates coordinates;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;

  @Column(name = "diameter", nullable = false, updatable = false)
  private int diameter;

  @Column(name = "temperature", nullable = false, updatable = false)
  private int temperature;

  @Column(name = "type", nullable = false, updatable = false)
  private BodyType type;

  @Column(name = "image", nullable = false)
  private int image;

  @Embedded
  private Resources resources;

  @Embedded
  private ProductionFactors productionFactors;

  @Column(name = "last_jump_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastJumpAt;

  @Column(name = "buildings", nullable = false)
  @Type(type = "int-array")
  private int[] buildingsArray;

  @Column(name = "units", nullable = false)
  @Type(type = "int-array")
  private int[] unitsArray;

  @OneToMany(mappedBy = "key.body")
  @MapKey(name = "key.sequence")
  @OrderBy("key.sequence")
  @Fetch(FetchMode.SUBSELECT)
  private SortedMap<Integer, BuildingQueueEntry> buildingQueue;

  @OneToMany(mappedBy = "key.body")
  @OrderBy("key.sequence")
  @Fetch(FetchMode.SUBSELECT)
  private List<ShipyardQueueEntry> shipyardQueue;

  public EnumMap<BuildingKind, Integer> getBuildings() {
    return ItemsSerialization.deserializeItems(BuildingKind.class, buildingsArray);
  }

  public void setBuildings(Map<BuildingKind, Integer> buildings) {
    buildingsArray = ItemsSerialization.serializeItems(BuildingKind.class, buildings);
  }

  public int getBuildingLevel(BuildingKind kind) {
    var index = kind.ordinal();
    var level = buildingsArray[index];
    assert level >= 0;
    return level;
  }

  public void setBuildingLevel(BuildingKind kind, int level) {
    assert level >= 0;
    var index = kind.ordinal();
    buildingsArray[index] = level;
  }

  public EnumMap<UnitKind, Integer> getUnits() {
    return ItemsSerialization.deserializeItems(UnitKind.class, unitsArray);
  }

  public void setUnits(Map<UnitKind, Integer> units) {
    unitsArray = ItemsSerialization.serializeItems(UnitKind.class, units);
  }

  public int getUnitsCount(UnitKind kind) {
    var index = kind.ordinal();
    var count = unitsArray[index];
    assert count >= 0;
    return count;
  }

  public void setUnitsCount(UnitKind kind, int count) {
    assert count >= 0;
    var index = kind.ordinal();
    unitsArray[index] = count;
  }

  public int getTotalUnitsCount() {
    return Arrays.stream(unitsArray).sum();
  }

  public long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getDiameter() {
    return diameter;
  }

  public void setDiameter(int diameter) {
    this.diameter = diameter;
  }

  public int getTemperature() {
    return temperature;
  }

  public void setTemperature(int temperature) {
    this.temperature = temperature;
  }

  public BodyType getType() {
    return type;
  }

  public void setType(BodyType type) {
    this.type = type;
  }

  public int getImage() {
    return image;
  }

  public void setImage(int image) {
    this.image = image;
  }

  public Resources getResources() {
    return resources;
  }

  public void setResources(Resources resources) {
    this.resources = resources;
  }

  public ProductionFactors getProductionFactors() {
    return productionFactors;
  }

  public void setProductionFactors(ProductionFactors productionFactors) {
    this.productionFactors = productionFactors;
  }

  public Date getLastJumpAt() {
    return lastJumpAt;
  }

  public void setLastJumpAt(Date lastJumpAt) {
    this.lastJumpAt = lastJumpAt;
  }

  public SortedMap<Integer, BuildingQueueEntry> getBuildingQueue() {
    return buildingQueue;
  }

  public List<ShipyardQueueEntry> getShipyardQueue() {
    return shipyardQueue;
  }
}
