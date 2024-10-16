#!/bin/bash
#loop Until cancled
while true; do
    a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'aliens@glassow.com';"); echo "Alien User ID is: $a_user_id"
    a_hw_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id order by id asc limit 1;") echo "Alien HW ID is: $a_hw_id"
    # Count Alien Moons
    a_moon_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and kind = 1;"); echo "Alien Moons: $a_moon_count"
    if [ $a_moon_count -gt 0 ]; then
        #Delete Moons
        m_delete=$(psql -d retro-game -U postgres -t -c "delete from bodies where user_id = $a_user_id and kind = 1;")
        psql -d retro-game -U postgres -t -c "$p_delete"; echo "Abandoning Alien Moon: $m_delete"
    fi
    sleep 1
    # Count Destroyed Outposts
    e_id=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and units = '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}' and name = 'OUTPOST';"); echo "Empty Outposts: $e_id"
    if [ $e_id -gt 0 ]; then
        # Renaming Abandoned Colonies
        rename=$(psql -d retro-game -U postgres -t -c "update bodies set name = 'Abandoned' where user_id = $a_user_id and units = '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}' and name = 'OUTPOST';")
        psql -d retro-game -U postgres -t -c "$rename"; echo "Abandoning Outpost: $e_id"
    fi
    sleep 1
    # Is there an abandoned colony?
    a_check_del_planet=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where name = 'Abandoned' and user_id = $a_user_id;"); echo "Counting Abandoned Outposts: $a_check_del_planet"
    if [ $a_check_del_planet -gt 0 ]; then
        echo "We have Abandoned Outposts!"; sleep 1
        # Get Abandoned ID
        del_planet=$(psql -d retro-game -U postgres -t -c "select id from bodies where name = 'Abandoned' and user_id = $a_user_id order by id desc limit 1;"); echo "Grabbing Planet for Delete- ID: $del_planet"
        # Check for Fleet
        f_check=$(psql -d retro-game -U postgres -t -c "select count(*) from flights where target_body_id = $del_planet;"); echo "Checking for flights: $f_check"
        # If No Fleet
        if [ $f_check -eq 0 ]; then
            # Delete planet
            p_delete=$(psql -d retro-game -U postgres -t -c "delete from bodies where id = $del_planet and user_id = $a_user_id;")
            psql -d retro-game -U postgres -t -c "$p_delete"; echo "Abandoning Alien Colony: $del_planet"
        else
        sleep 1
            echo "There was a flight!"; sleep 1
            m_flights_check=$(psql -d retro-game -U postgres -t -c "select count(*) from flights where target_body_id = $del_planet and start_user_id = $a_user_id;"); echo "Checking for MY flights: $m_flights_check"
            if [ $m_flights_check -gt 0 ]; then
                echo "I have flights to delete.."; sleep 1
                # Check for Alien Fleet
                a_fleet_id=$(psql -d retro-game -U postgres -t -c "select id from flights where start_user_id = $a_user_id and target_body_id = $del_planet;"); echo "Checking Flights to: $del_planet"
                # Delete Alien Fleet Events
                e_delete=$(psql -d retro-game -U postgres -t -c "delete from events where param = $a_fleet_id;")
                psql -d retro-game -U postgres -t -c "$e_delete"; echo "Abandoning Alien Colony: $del_planet"
                # Delete Alien Fleets
                f_delete=$(psql -d retro-game -U postgres -t -c "delete from flights where id = $a_fleet_id and start_user_id = $a_user_id;")
                psql -d retro-game -U postgres -t -c "$f_delete"; echo "Abandoning Alien Colony: $del_planet"
            fi
            sleep 1
        fi
        sleep 1
    else
        echo "There are no abandoned outposts!"; sleep 1
    fi
    sleep 1
    # Get #1 Player Fleet Score
    t_player_score=$(psql -d retro-game -U postgres -t -c "select points from fleet_statistics where at = (select at from fleet_statistics order by at desc limit 1) and user_id not in (1, 2, 3, 4) order by rank asc limit 1;")
    # Divide by 10 and grab player count above that.
    m_fleet_score=$(( t_player_score / 10 )); echo "Minimum Fleeter Score: $m_fleet_score"
    min_fleet_count=$(psql -d retro-game -U postgres -t -c "select count(*) from fleet_statistics where at = (select at from fleet_statistics order by at desc limit 1) and user_id not in (1, 2, 3, 4) and points > $m_fleet_score;")
    while [ $min_fleet_count -gt 0 ]; do
        min_fleet_count=$((min_fleet_count-1))
        # Get most recent alien bodies
        r_body_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id order by id desc limit 1;"); echo "Grabbing Most Recent colo: $r_body_id"
        # SELECT * FROM bodies ORDER BY id LIMIT 1 OFFSET 1; ## Not sure what this does
        # Check Units on that body
        u_check=$(psql -d retro-game -U postgres -t -c "select units from bodies where user_id = $a_user_id and id = $r_body_id;"); echo "Checking for Units: $u_check"
        # Format First
        u_check="${u_check::-1}" # Remove the last }
        u_check="${u_check:1}" # Remove the first {}
        u_check="${u_check:1}" # I don't know why, but i have to do this twice
        echo $u_check; sleep 1
        # Convert my string to an array
        IFS=',' read -ra units_array <<< "$u_check"; echo "${units_array[12]}"  
        # Update the 12th number if it's greater than 0
        if [ "${units_array[12]}" -gt 0 ]; then
            units_array[12]=0 # Change the 12th number to the desired value
            echo "I'm Throwing Away RIPS!"
            # Assemble them back into the original format
            new_units="{$(IFS=,; echo "${units_array[*]}")}"; echo "$new_units"
            a_unit_update=$(psql -d retro-game -U postgres -t -c "update bodies set units = '$new_units' where id = $r_body_id and user_id = $a_user_id;")
            psql -d retro-game -U postgres -t -c "$a_unit_update"; echo "Updating Units: $new_units"
        else
            sleep 1
            echo "There are no RIPS, someone needs to attack it!"
        fi
        sleep 1
    done
done