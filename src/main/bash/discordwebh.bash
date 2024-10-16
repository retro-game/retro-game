#!/bin/bash
# Set the PostgreSQL connection parameters
export PGHOST="localhost"
export PGPORT="5432"
export PGDATABASE="retro-game"
export PGUSER="postgres"
export PGPASSWORD=""
# Set your Discord webhook URL here
WEBHOOK_URL="https://discord.com/api/webhooks/1087806999099613195/OWSOGOcbaTdDtW54I0L2OU8iBMetmkX8kpfMoeO3uH6YJPnpvl9KlONiOoWKkQfnB087"
# Set your message content here
MESSAGE="An allien mothership has entered the galaxy!"
# Build the JSON payload for the webhook request
PAYLOAD="{\"content\":\"$MESSAGE\"}"

#Determine Spawn Type
case $((RANDOM % 3 + 1)) in
  1) echo "1 means Aliens.";;
  2) echo "2 means Pirates.";;
  3) echo "3 means Envoys.";;
esac
# Pick Random Location
Galaxy=$((RANDOM % 5 + 1))
System=$((RANDOM % 499 + 1))
Position=$((RANDOM % 15 + 1))
echo "$Galaxy:$System:$Position"
# confirm if location is available
id=$(psql -d retro-game -U postgres -t -c "select id from bodies where galaxy = $Galaxy and system = $System and position = $Position;")
echo $id
if [ -z "$id" ]; then
  echo "No rows found."
else
  echo "Result: $id"
  # Perform your action here if the result is not 0
fi


# Get high score data
# select * from overall_statistics where at = (select at from overall_statistics order by at asc limit 1) order by rank asc;


# Insert into bodies 

# Send the webhook request using cURL
curl -H "Content-Type: application/json" -d "$PAYLOAD" "$WEBHOOK_URL"

# Execute the SQL query using psql
psql -d dbname -U username -c "$sql_query"

# Connect to the PostgreSQL database and execute a SELECT statement
id=$(psql -d retro-game -U postgres -t -c "select max(id) from bodies;")
id=$(psql -d retro-game -U postgres -t -c "select id from bodies where galaxy = 5 and system = 312 and position = 12;")
echo $id
if [ -z "$id" ]; then
  echo "No rows found."
else
  echo "Result: $id"
  # Perform your action here if the result is not 0
fi




((id++))
# specify query
sql_query="SELECT name FROM bodies WHERE id = 100;"
# execute query
query_result=$(psql -d "retro-game" -U "postgres" -w -c "$sql_query" -t)

# Print the value to the console
echo $query_result
echo $id

# Define the SQL command to add the new column
SQL_COMMAND="update bodies set buildings = '{19,15,13,20,3,6,0,7,0,0,0,7,0,0,0,0,0,0}' where id = 23;"
# create alien planet insert into bodies (id, user_id, galaxy, system, position, kind, name, created_at, updated_at, diameter, temperature, type, image, metal, crystal, deuterium, metal_mine_factor, crystal_mine_factor, deuterium_synthesizer_factor, solar_plant_factor, fusion_reactor_factor, solar_satellites_factor, buildings, units, building_queue, shipyard_queue) VALUES (100, 2, 5, 5, 5, 0, 'Mothership', '2023-03-19 22:00:00+00', '2023-03-22 00:10:11+00', 18000, 0, 5, 8, 5000, 5000, 5000, 0, 0, 0, 0, 0, 0, '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}', '{500,500,500,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0}', '{}', '{}');

# Execute the SQL command using the psql tool
psql -c "$SQL_COMMAND"