# An Incursion Begins...
# Specify body ID of homeworld
a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'aliens@glassow.com;'); echo "Alien User ID is: $a_user_id"
a_homeworld_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id order by id asc limit 1;") echo "Alien HW ID is: $a_homeworld_id"
# Anounce Incursion To Discord
echo "Anouncing on Discord"
curl -H "Content-Type: application/json" -d '{"content":"An Alien Incursion Has Begun!!"}' https://discord.com/api/webhooks/1096872402195062994/VL_v9Wfs6iDmIL-sRsd-RbSviA7RXISDsGMxO4syUQwF6FWkORHOLAqFXzZQoKYW4Qhf
#echo "Sleeping for random Dely"; sleep $((RANDOM % 3600 + 1))

# Create Alien Mothership
q_rename="update bodies set name = 'Mothership' where id = $a_homeworld_id;"
f_update="update bodies set units = '{0,0,0,0,0,0,0,0,0,0,0,0,10000,0,0,0,0,0,0,0,0,0,0}' where id = $a_homeworld_id;"
psql -d retro-game -U postgres -t -c "$q_rename"
psql -d retro-game -U postgres -t -c "$f_update"

# Launch other scripts
tmux kill-session -t envoy; echo "Ending Envoys"
tmux new-session -d -s inc-colonizer './inc-colonizer.sh'; echo "Starting Colonizer"
tmux new-session -d -s inc-res-maker './inc-res-maker.sh'; echo "Starting Res Maker"
tmux new-session -d -s inc-cleanup './inc-cleanup.sh'; echo "Starting Cleanup"
tmux new-session -d -s inc-anounce './inc-prog-ann.sh'; echo "Starting Anouncer"

