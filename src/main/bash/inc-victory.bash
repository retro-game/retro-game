#!/bin/bash
# Configure Discord Webhook
webhook_url="https://discord.com/api/webhooks/1096872402195062994/VL_v9Wfs6iDmIL-sRsd-RbSviA7RXISDsGMxO4syUQwF6FWkORHOLAqFXzZQoKYW4Qhf"
# Define Message
input="Aliens have been defeated! Incursion is over..."
# Send MEssage
#curl -H "Content-Type: application/json" -X POST -d '{"content": "'"$input"'"}' $webhook_url
# Define Message
input2="The debris of the alien empire slowly drifts toward each celestial body in the universe. Your engineers have devised a way to begin salvaging this technology to grant resources to your empire!"
# Send MEssage
#curl -H "Content-Type: application/json" -X POST -d '{"content": "'"$input2"'"}' $webhook_url
# Configure Reward Resources
metal=1500
crystal=1000
deuterium=500
# Configure Bonus Time
run_count=$((7 * 24 * 60)); echo "Determing Run Count: $run_count"
while [ $run_count -gt 0 ]; do
    echo "Count was: $run_count"
    run_count=$((run_count-1)); echo "Count is now: $run_count"
    win_res="update bodies set metal = metal + $metal, crystal = crystal + $crystal, deuterium = deuterium + $deuterium ;"
    psql -d retro-game -U postgres -t -c "$win_res"
    sleep 60
done
# Define Message
input3="Your engineers report, all alien debris has been harvested, we have gathered a total of 30 million resources!"
# Send MEssage
curl -H "Content-Type: application/json" -X POST -d '{"content": "'"$input3"'"}' $webhook_url