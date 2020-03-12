package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.UnitKindDto;
import com.github.retro_game.retro_game.entity.UnitKind;
import com.github.retro_game.retro_game.service.RapidFireTableService;
import com.github.retro_game.retro_game.service.impl.item.unit.UnitItem;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Service
class RapidFireTableServiceImpl implements RapidFireTableService {
  private final Map<UnitKindDto, Map<UnitKindDto, Integer>> rapidFireTable;

  public RapidFireTableServiceImpl() {
    rapidFireTable = Collections.unmodifiableMap(generateRapidFireTable());
  }

  private Map<UnitKindDto, Map<UnitKindDto, Integer>> generateRapidFireTable() {
    Map<UnitKindDto, Map<UnitKindDto, Integer>> rapidFireTable = new EnumMap<>(UnitKindDto.class);

    Map<UnitKind, UnitItem> all = UnitItem.getAll();
    for (Map.Entry<UnitKind, UnitItem> rowEntry : all.entrySet()) {
      UnitKind rowKind = rowEntry.getKey();
      Map<UnitKind, Integer> rapidFireAgainst = rowEntry.getValue().getRapidFireAgainst();

      Map<UnitKindDto, Integer> rf = new EnumMap<>(UnitKindDto.class);
      for (Map.Entry<UnitKind, UnitItem> colEntry : all.entrySet()) {
        UnitKind colKind = colEntry.getKey();

        Integer n = rapidFireAgainst.get(colKind);
        if (n != null) {
          rf.put(Converter.convert(colKind), n);
          continue;
        }

        final Map<UnitKind, Integer> rapidFireFrom = colEntry.getValue().getRapidFireAgainst();
        n = rapidFireFrom.get(rowKind);
        if (n != null) {
          rf.put(Converter.convert(colKind), -n);
          continue;
        }

        rf.put(Converter.convert(colKind), 0);
      }

      rapidFireTable.put(Converter.convert(rowKind), rf);
    }

    return rapidFireTable;
  }

  @Override
  public Map<UnitKindDto, Map<UnitKindDto, Integer>> getRapidFireTable(long bodyId) {
    return rapidFireTable;
  }
}
