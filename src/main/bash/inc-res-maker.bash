#!/bin/bash
a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'aliens@glassow.com';"); echo "Alien User ID is: $a_user_id"
# loop until stopped
while true; do
  sleep 900
  querym="update bodies set metal = metal + 175 where User_id = $a_user_id;"
  queryc="update bodies set crystal = crystal + 120 where User_id = $a_user_id;"
  queryd="update bodies set deuterium = deuterium + 60 where User_id = $a_user_id;"
  psql -d retro-game -U postgres -t -c "$querym"
  psql -d retro-game -U postgres -t -c "$queryc"
  psql -d retro-game -U postgres -t -c "$queryd"
done

