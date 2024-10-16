package com.github.retro_game.retro_game.entity;

import java.util.EnumMap;
import java.util.Map;

class SerializationUtils {
  static <K extends Enum<K>> EnumMap<K, Integer> deserializeItems(Class<K> clazz, int[] array) {
    var enumConstants = clazz.getEnumConstants();
    assert enumConstants.length == array.length;
    var items = new EnumMap<K, Integer>(clazz);
    for (var i = 0; i < array.length; i++) {
      var kind = enumConstants[i];
      var value = array[i];
      assert value >= 0;
      items.put(kind, value);
    }
    return items;
  }

  static <K extends Enum<K>> int[] serializeItems(Class<K> clazz, Map<K, Integer> items) {
    var enumConstants = clazz.getEnumConstants();
    var array = new int[enumConstants.length];
    for (var entry : items.entrySet()) {
      var index = entry.getKey().ordinal();
      var value = entry.getValue();
      assert value >= 0;
      array[index] = value;
    }
    return array;
  }
}
