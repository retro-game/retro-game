#!/bin/bash
#loop Until cancled
while true; do
    a_user_id=$(psql -d retro-game -U postgres -t -c "select id from users where email = 'envoys@glassow.com';"); echo "Envoy User ID is: $a_user_id"
    sleep 1
    #a_hw_id=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = 4 order by id asc limit 1;") echo "Envoy HW ID is: $a_hw_id"
    a_hw_id=31064; echo "Envoy HW Id is: $a_hw_id"
    # Count Alien Moons
    a_moon_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and kind = 1;"); echo "Envoy Moons: $a_moon_count"
    if [ $a_moon_count -gt 0 ]; then
        #Delete Moons
        m_delete=$(psql -d retro-game -U postgres -t -c "delete from bodies where user_id = $a_user_id and kind = 1;")
        psql -d retro-game -U postgres -t -c "$p_delete"; echo "Abandoning Envoy Moon: $m_delete"
    fi
    sleep 1
    # Count Destroyed Planets
    e_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and units = '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}';"); echo "Empty Planets: $e_count"
    sleep 1
    # Is there an empty planet?
    a_check_del_planet=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and id not in (31064);"); echo "Counting Abandoned Outposts: $a_check_del_planet"
    if [ $a_check_del_planet -gt 0 ]; then
        echo "We have Settlements!"; sleep 1
        # Roll D60
        d60=$(( ( RANDOM % 61 ) + 20 ))
        # Get Random ID
        del_planet=$(psql -d retro-game -U postgres -t -c "select id from bodies where user_id = $a_user_id and id not in (31064) ORDER BY RANDOM() limit 1;"); echo "Grabbing Planet for Delete- ID: $del_planet"
        # Check for Fleet
        f_check=$(psql -d retro-game -U postgres -t -c "select count(*) from flights where target_body_id = $del_planet;"); echo "Checking for flights: $f_check"
        # If No Fleet
        if [ $f_check -eq 0 ]; then
            # Delete planet
            echo "There are no flights, deleting planet..."
            #p_delete=$(psql -d retro-game -U postgres -t -c "delete from bodies where id = $del_planet and user_id = $a_user_id;")
            p_delete=$(psql -d retro-game -U postgres -t -c "delete from bodies where id = $del_planet and user_id = 4;")
            psql -d retro-game -U postgres -t -c "$p_delete"; echo "Abandoning Envoy Colony: $del_planet"
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
    #Random Sleeper
    e_planets=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = $a_user_id and units = '{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}';"); echo "Empty Envoy Planets: $m_flights_check"
    r_sleeper=$(( $d60 * $d60 * $d60 / $e_planets )); echo "Sleeping $r_sleeper"
    sleep $r_sleeper
done