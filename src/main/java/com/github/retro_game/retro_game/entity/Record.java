package com.github.retro_game.retro_game.entity;

import com.vladmihalcea.hibernate.type.array.LongArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "records")
@TypeDef(name = "long-array", typeClass = LongArrayType.class)
public class Record {
  @Column(name = "key")
  @Id
  private String key;

  @Column(name = "value", nullable = false)
  private long value;

  @Column(name = "at", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "holders", nullable = false)
  @Type(type = "long-array")
  private long[] holders;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public Date getAt() {
    return at;
  }

  public void setAt(Date at) {
    this.at = at;
  }

  public long[] getHolders() {
    return holders;
  }

  public void setHolders(long[] holders) {
    this.holders = holders;
  }
}
