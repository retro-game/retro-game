while true; do
  # Used for testing
  #p_id=2
  # Grab a random player
  p_id=$(psql -d retro-game -U postgres -t -c "select id from users where id NOT in (1, 2, 3, 4) order by RANDOM() limit 1;")
  p_name=$(psql -d retro-game -U postgres -t -c "select name from users where id = $p_id;"); echo "INFO : Selected $p_name for envoy"
  # grab the #1 ranked players points
  r_one_points=$(psql -d retro-game -U postgres -t -c "select points from overall_statistics where at = (select at from overall_statistics order by at desc limit 1) order by rank asc limit 1;")
  # Grab the number of players
  a_players=$(psql -d retro-game -U postgres -t -c "select rank from overall_statistics order by rank desc limit 1;")
  # Grab the random players rank
  p_rank=$(psql -d retro-game -U postgres -t -c "select rank from overall_statistics where user_id = $p_id;")
  # Grab the random players points
  p_points=$(psql -d retro-game -U postgres -t -c "select points from overall_statistics where user_id = $p_id and at = (select at from overall_statistics order by at desc limit 1);")
  if [ "$p_points" -eq 0 ]; then
    echo "WARNING : Player points value is equal to 0. Selecting a new player who has actually done something..."
    continue
  fi
  # Calculate gift
  metal=$(echo "($p_points*5)" | bc);       echo "INFO : Economy Bonus - Metal - $metal"
  crystal=$(echo "($p_points*3.33)" | bc);  echo "INFO : Economy Bonus - Crystal - $crystal"
  deut=$(echo "($p_points*1.66)" | bc);     echo "INFO : Economy Bonus - Deut - $deut"
  # calculate coef
  coef=$(echo "scale=2;( ($r_one_points - $p_points) / $p_points )" | bc); echo "INFO : I will now multiply res by - $coef"
  coef=$(printf "%0.2f" $coef)
  # calculate new gift
  metal=$(printf "%.2f" $(echo "$metal * $coef" | bc -l));     echo "INFO : Calculating New Metal -  $metal"
  crystal=$(printf "%.2f" $(echo "$crystal * $coef" | bc -l)); echo "INFO : Calculating New Crystal -  $crystal"
  deut=$(printf "%.2f" $(echo "$deut * $coef" | bc -l));       echo "INFO : Calculating New Deut - $deut"
  # determine number of cargos
  #r_sum=$(($metal + $crystal + $deut)); echo $r_sum
  #r_sum=$(printf "%d" $r_sum)
  #l_cargo=$(echo "scale=4; $r_sum / 20000" | bc)
  #l_cargo=$(printf "%d" $l_cargo); echo $l_cargo
  # Get User Body
  b_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $p_id and kind = 0 order by RANDOM() limit 1;")
  # get body Coords
  b_galaxy=$(psql -d retro-game -U postgres -t -c "select galaxy from bodies where id = $b_id;");
  b_system=$(psql -d retro-game -U postgres -t -c "select system from bodies where id = $b_id;");
  b_position=$(psql -d retro-game -U postgres -t -c "select position from bodies where id = $b_id;");
  echo "INFO : I'm creating a new flight from body - $b_id | $b_galaxy : $b_system : $b_position"
  # Create New Flight
  envoy_body=31063
  query1="INSERT INTO flights (start_user_id, start_body_id, target_user_id, target_body_id, target_galaxy, target_system, target_position, target_kind, departure_at, arrival_at, return_at, mission, metal, crystal, deuterium, units) VALUES ($p_id, $b_id, 4, $envoy_body, 3, 250, 2, 1, '2025-01-01 00:00:00+00', '2026-01-01 00:00:00+00', '2023-04-27 05:00:00+00', 7, $metal, $crystal, $deut, '{0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0}');"
  #(4, 160, $p_id, $b_id, $b_galaxy, $b_system, $b_position, 0, now(), (now() + interval '120 minutes'), (now() + interval '121 minutes'), 7, $metal, $crystal, $deut, '{0,25000,250,100,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}');  
  query2="insert into events (at, kind, param) values ((select arrival_at from flights where target_user_id = 4 order by id desc limit 1), 3, (select id from flights where target_user_id = 4 order by id desc limit 1));"
  #message="Hello, we are from another world. As an offering to your supreme being, we have delivered $metal metal, $crystal crystal and $deut deuterium to your planet at $location"
  #query3="insert into private_messages (sender_id, recipient_id, deleted_by_sender, deleted_by_recipient, at, message)VALUES (4, $p_id, 'f', 'f', now(), '$message')"
  echo "INFO : First I will Create your Flight"
  psql -d retro-game -U postgres -t -c "$query1" || break
  echo "INFO: Next, I make sure it will return on time"
  psql -d retro-game -U postgres -t -c "$query2" || break
  #echo "Inserting PM"
  #psql -d retro-game -U postgres -t -c "$query3" || break
  # Take a random break between missions that dynamically adjusts based on player count such that each player sees ~3 missions for week. 
  s_time=$(expr 800000 / $a_players)
  g_time=$((RANDOM%300-$a_players))
  sleep_time=$((s_time + g_time)); echo "INFO : Sleeping for $sleep_time seconds..."
  sleep $sleep_time
done
