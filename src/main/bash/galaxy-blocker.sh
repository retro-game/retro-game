#!/bin/bash
a_galaxy=3
a_system=1
a_position=1

while [ $a_system -le 500 ]; do
   while [ $a_position -le 15 ]; do
      query="insert into bodies (user_id, galaxy, system, position, kind, name, created_at, updated_at, diameter, temperature, type, image, metal, crystal, deuterium, metal_mine_factor, crystal_mine_factor, deuterium_synthesizer_factor, solar_plant_factor, fusion_reactor_factor, solar_satellites_factor, last_jump_at, buildings, units, building_queue, shipyard_queue) values (1, $a_galaxy, $a_system, $a_position, 0, 'BLOCKED', NOW(), NOW(), 10000, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, NOW(), '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}', '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}', '{}', '{}');"
      psql -d password -U username -t -c "$query"
      echo "Creating: $a_galaxy, $a_system, $a_position"    
      a_position=$((a_position+1))
   done
   a_position=1
   a_system=$((a_system+1))
done
