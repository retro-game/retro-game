#!/bin/bash
a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'aliens@glassow.com';"); echo "Alien User ID is: $a_user_id"
a_hw_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id order by id asc limit 1;") echo "Alien HW ID is: $a_hw_id"
#loop until stopped
while true; do
  # Grab the number of planets
  p_planets=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id not in (1,2,3,4);"); echo "Grabbing Planets: $p_planets"
  # Calculate a 5 day doomsday based on Player Planet Count
  c_timer=$(( (4 * 5 * 24 * 60 * 60) / p_planets )); echo "Calculating Sleep Timer: $c_timer"
  w_count=0
  while [ $w_count -le 4 ]; do
    w_count=$((w_count+1))
    echo "The Count is now: $w_count"
    sleep 2
    # grab a random player's location
    r_planet=$(psql -d retro-game -U postgres -t -c "SELECT id FROM bodies where kind = 0 and user_id not in (1,2,3,4) ORDER BY RANDOM() LIMIT 1;");   echo "Grabbing Target Player Planet: $r_planet"
    p_galaxy=$(psql -d retro-game -U postgres -t -c "SELECT galaxy FROM bodies where id = $r_planet;")
    p_system=$(psql -d retro-game -U postgres -t -c "SELECT system FROM bodies where id = $r_planet;")
    p_position=$(psql -d retro-game -U postgres -t -c "SELECT position FROM bodies where id = $r_planet;"); echo "Grabbing Target Player Planet Coords: $p_galaxy : $p_system : $p_position"

    # pick a spot near it
    # Generate a random number between -25 and 25
    offset=$((RANDOM % 51 - 25)); echo "Doing Random Number Things: $offset"

    # add the offset
    a_system=$(($p_system + $offset)); echo "Picking new Alien System For Colo: $a_system"

    # Check if the result is negative and add 500 if it is
    if ((a_system < 0)); then
      sleep 1
      echo "Oh no, system is negative! Ajusting for Doughnut Galaxy..."
      a_system=$((a_system + 500))
      sleep 1
    fi
    if ((a_system > 500)); then
      sleep 1
      echo "Oh no, system is too high! Ajusting for Doughnut Galaxy..."
      a_system=$((a_system - 500))
      sleep 1
    fi
    echo "Alien System: $a_system"

    # Check if alien position is open
    count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where galaxy = $p_galaxy and system = $a_system and position = $p_position;"); echo "False/True is there a planet there: $count"
    # if occupied, restart loop
    if [ $count -gt 0 ]; then
      echo "True, trying again..."
      continue
    fi
    # grab 4thth players fleet score
    t_fleet=$(psql -d retro-game -U postgres -t -c "SELECT points FROM fleet_statistics ORDER BY at DESC, points DESC LIMIT 1 OFFSET 3;"); echo "Grabbing Fleet Score: $t_fleet"

    # create a randomly generated fleet based on that score
    fleet_score=$(printf "%.0f" $(echo "$t_fleet / 30" | bc -l)); echo "Score is: $fleet_score"
    sc=4; lc=12; lf=4; hf=10; cr=29; bs=60; ep=1; cs=40; ds=125; bb=85

    # Calculate random numbers for each variable
    while true; do
      rsc=$((RANDOM % (fleet_score + 1)))
      rlc=$((RANDOM % (fleet_score - rsc + 1)))
      rlf=$((RANDOM % (fleet_score - rsc - rlc + 1)))
      rhf=$((RANDOM % (fleet_score - rsc - rlc - rlf + 1)))
      rbs=$((RANDOM % (fleet_score - rsc - rlc - rlf - rhf + 1)))
      rep=$((RANDOM % (fleet_score - rsc - rlc - rlf - rhf - rbs + 1)))
      rcr=$((RANDOM % (fleet_score - rsc - rlc - rlf - rhf - rbs - rep - rrip + 1)))
      rcs=$((RANDOM % (fleet_score - rsc - rlc - rlf - rhf - rbs - rep - rrip - rcr + 1)))
      rds=$((RANDOM % (fleet_score - rsc - rlc - rlf - rhf - rbs - rep - rrip - rcr - rcs + 1)))
      rbb=$((fleet_score - rsc - rlc - rlf - rhf - rbs - rep - rrip - rcr - rcs - rds))

      # Check if the sum of random numbers is equal to fleet_score
      if [ $((rsc + rlc + rlf + rhf + rbs + rep + rcr + rcs + rds + rbb)) -eq $fleet_score ]; then
        break
      fi
      sleep 1
    done
    # Save each value to an array and format it as a string
    arr=()
    arr+=("$rsc")
    arr+=("$rlc")
    arr+=("$rlf")
    arr+=("$rhf")
    arr+=("$rcr")
    arr+=("$rbs")
    arr+=("$rcs")
    arr+=("0") #recyclers
    arr+=("$rep") #Probes
    arr+=("$rbb") #bombers
    arr+=("25") # sats
    arr+=("$rds") # Destroyers
    arr+=("0") # Rips
    arr+=("0") # RL
    arr+=("0") # LL
    arr+=("0") # HL
    arr+=("0") # Gaus
    arr+=("0") # Ion
    arr+=("0") # Plasm
    arr+=("0") # SSD
    arr+=("0") # LSD
    arr+=("0") # Anti-B
    arr+=("0") # Missile
    #Format the array
    new_str="{${arr[0]}"
    for i in "${arr[@]:1}"; do
        new_str+=",${i}"
    done
    # Display the array
    new_str+="}'"
    new_str="'${new_str}"
    echo "$new_str"
    #sleep 40
    
    # populate a planet
    query="insert into bodies (user_id, galaxy, system, position, kind, name, created_at, updated_at, diameter, temperature, type, image, metal, crystal, deuterium, metal_mine_factor, crystal_mine_factor, deuterium_synthesizer_factor, solar_plant_factor, fusion_reactor_factor, solar_satellites_factor, last_jump_at, buildings, units, building_queue, shipyard_queue) values ($a_user_id, $p_galaxy, $a_system, $p_position, 0, 'OUTPOST', NOW(), NOW(), 10000, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, NOW(), '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}', $new_str, '{}', '{}');"
    psql -d retro-game -U postgres -t -c "$query" || break
    # Get Planet ID
    sleep 1
    b_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = 2 order by created_at desc limit 1;"); echo "Planet ID: $b_id"
    # Set Deployment Time
    int_min_a=120
    int_min_r=240
    # Create Deployment
    echo 'Creating Deployment'
    queryF="insert into flights (start_user_id, start_body_id, target_user_id, target_body_id, target_galaxy, target_system, target_position, target_kind, departure_at, arrival_at, return_at, mission, metal, crystal, deuterium, units) values ($a_user_id, 31063, $a_user_id, $b_id, $p_galaxy, $a_system, $p_position, 0, NOW(), (now() + interval '$int_min_a minutes'), (now() + interval '$int_min_r minutes'), 2, 0, 0, 0, '{0,0,0,0,0,0,0,0,0,0,0,0,10000,0,0,0,0,0,0,0,0,0,0}');"
    psql -d retro-game -U postgres -t -c "$queryF" || break
    # Get Flight ID
    fl_id=$(psql -d retro-game -U postgres -t -c "select id from flights where start_user_id = $a_user_id order by departure_at desc limit 1;"); echo "Fleet ID: $fl_id"
    # Create Deploy Event
    sleep 1
    a_time=$(psql -d retro-game -U postgres -t -c "select arrival_at from flights where id = $fl_id;"); echo "Arival is: $a_time"
    queryE="insert into events (at, kind, param) values ((now() + interval '90 minutes'), 3, $fl_id);"
    psql -d retro-game -U postgres -t -c "$queryE" || break
    # sleep for derived seconds
  done
  echo "Sleeping..."
  sleep $c_timer
done