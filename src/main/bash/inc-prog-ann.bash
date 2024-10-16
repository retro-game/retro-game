#!/bin/bash
a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'aliens@glassow.com';"); echo "Alien User ID is: $a_user_id"
a_hw_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id order by id asc limit 1;") echo "Alien HW ID is: $a_hw_id"
# loop until stopped
while true; do
  # Count Player Planets
  p_planets=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id not in (1, 2, 3, 4);"); echo "There are $p_planets Player Planets"
  # Count Alien Planets
  a_planets=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id;"); echo "There are $a_planets Alien Planets"
  # Do Maths
  i_perc=$(echo "scale=2; $a_planets / $p_planets * 100" | bc); echo "$i_perc%"
  # Define message
  input="Aliens have$a_planets outposts! Incursion is $i_perc % complete..."
  webhook_url="https://discord.com/api/webhooks/1096872402195062994/VL_v9Wfs6iDmIL-sRsd-RbSviA7RXISDsGMxO4syUQwF6FWkORHOLAqFXzZQoKYW4Qhf"
  # Add message to discourd 
  curl -H "Content-Type: application/json" -X POST -d '{"content": "'"$input"'"}' $webhook_url
  sleep 21600
done


