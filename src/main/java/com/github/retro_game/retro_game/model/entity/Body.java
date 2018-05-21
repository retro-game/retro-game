package com.github.retro_game.retro_game.model.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Entity
@Table(name = "bodies")
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

  @OneToMany(mappedBy = "key.body")
  @MapKey(name = "key.kind")
  @Fetch(FetchMode.SUBSELECT)
  private Map<BuildingKind, Building> buildings;

  @OneToMany(mappedBy = "key.body")
  @MapKey(name = "key.kind")
  private Map<UnitKind, BodyUnit> units;

  @OneToMany(mappedBy = "key.body")
  @MapKey(name = "key.sequence")
  @OrderBy("key.sequence")
  @Fetch(FetchMode.SUBSELECT)
  private SortedMap<Integer, BuildingQueueEntry> buildingQueue;

  @OneToMany(mappedBy = "key.body")
  @OrderBy("key.sequence")
  private List<ShipyardQueueEntry> shipyardQueue;

  public int getBuildingLevel(BuildingKind kind) {
    Building building = buildings.get(kind);
    return building != null ? building.getLevel() : 0;
  }

  public int getNumUnits(UnitKind kind) {
    BodyUnit unit = units.get(kind);
    return unit != null ? unit.getCount() : 0;
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

  public Map<BuildingKind, Building> getBuildings() {
    return buildings;
  }

  public Map<UnitKind, BodyUnit> getUnits() {
    return units;
  }

  public SortedMap<Integer, BuildingQueueEntry> getBuildingQueue() {
    return buildingQueue;
  }

  public List<ShipyardQueueEntry> getShipyardQueue() {
    return shipyardQueue;
  }
}
