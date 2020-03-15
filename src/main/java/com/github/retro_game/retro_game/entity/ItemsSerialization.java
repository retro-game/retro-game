package com.github.retro_game.retro_game.entity;

import java.util.EnumMap;
import java.util.Map;

class ItemsSerialization {
  static <K extends Enum<K>> EnumMap<K, Integer> deserializeItems(Class<K> clazz, int[] array) {
    var enumValues = clazz.getEnumConstants();
    assert enumValues.length == array.length;
    var items = new EnumMap<K, Integer>(clazz);
    for (var i = 0; i < array.length; i++) {
      var kind = enumValues[i];
      var value = array[i];
      assert value >= 0;
      items.put(kind, value);
    }
    return items;
  }

  static <K extends Enum<K>> int[] serializeItems(Class<K> clazz, Map<K, Integer> items) {
    var enumValues = clazz.getEnumConstants();
    var array = new int[enumValues.length];
    for (var entry : items.entrySet()) {
      var index = entry.getKey().ordinal();
      var value = entry.getValue();
      assert value >= 0;
      array[index] = value;
    }
    return array;
  }
}
