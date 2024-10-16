#!/bin/bash
#Table of all user resources
user_3=1647472000
user_9=980718000
user_12=640720000
user_34=438796000
user_62=63880000
user_54=31310000
user_29=13484000
user_56=12325000
user_30=8614000
user_14=8054000
user_8=7469000
user_20=6174000
user_42=4401000
user_16=3682000
user_6=3474000
user_17=3188000
user_23=3151000
user_19=3094000
user_24=2825000

user_5=2331704000       #Grim             654 042 972
user_10=3220050000      #Jorj             903 224 025
user_11=3887178000      #Stilgar        1 090 353 429
user_13=4010490000      #Gunslinger     1 124 942 445
user_33=555377000       #PistolPete       155 783 248
user_28=5417082000      #CuZZer         1 519 491 501

# loop until stopped
while true; do
    # Get User Planet Count
    Grim_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 5 and kind = 0;");
    Jorj_X_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 10 and kind = 0;");
    Stilgar_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 11 and kind = 0");
    Gunslinger_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 13 and kind = 0");
    PistolPete_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 33 and kind = 0;");
    CuZZer_count=$(psql -d retro-game -U postgres -t -c "select count(*) from bodies where user_id = 28 and kind = 0;");
    echo "Grim Count = $Grim_count" 
    # Divide Res by Minutes
    d_minutes=600000
    grim_res=$(( 654042972 / $d_minutes ))
    jorj_res=$(( 903224025 / $d_minutes ))
    stilgar_res=$(( 1090353429 / $d_minutes ))
    gunslinger_res=$(( 1124942445 / $d_minutes ))
    pistolpete_res=$(( 155783248 / $d_minutes ))
    cuzzer_res=$(( 1519491501 / $d_minutes ))
    echo "1 - Cuzzer     Total Res is $cuzzer_res" 
    echo "2 - Gunslinger Total Res is $gunslinger_res"
    echo "3 - Stilger    Total Res is $stilgar_res"
    echo "4 - Jorj X     Total Res is $jorj_res"
    echo "5 - Grim       Total Res is $grim_res"
    echo "6 - PistolPete Total Res is $pistolpete_res"
    # Divide by planet count to get distribution amount
    rm_grim_res=$(( $grim_res / $Grim_count ))
    m_jorj_res=$(( $jorj_res / $Jorj_X_count ))
    m_stilgar_res=$(( $stilgar_res / $Stilgar_count ))
    m_gunslinger_res=$(( $gunslinger_res / $Gunslinger_count ))
    m_pistolpete_res=$(( $pistolpete_res / $PistolPete_count ))
    m_cuzzer_res=$(( $cuzzer_res / $CuZZer_count ))
    echo "Cuzzer distribution is $m_cuzzer_res per planet" 
    echo "Gunslinger distribution is $m_gunslinger_res per planet" 
    echo "stilgar distribution is $m_stilgar_res per planet" 
    echo "jorj distribution is $m_jorj_res per planet" 
    echo "Grim distribution is $rm_grim_res per planet" 
    echo "PistolPete distribution is $m_pistolpete_res per planet" 
    
    # Distribute Resources
    grim_dist="         update bodies set metal = metal + ($rm_grim_res * 3),       crystal = crystal + ($rm_grim_res * 2),       deuterium = deuterium + ($rm_grim_res * 1)        where user_id = 5 and kind = 0;"
    jorj_dist="         update bodies set metal = metal + ($m_jorj_res * 3),        crystal = crystal + ($m_jorj_res * 2),        deuterium = deuterium + ($m_jorj_res * 1)         where user_id = 10 and kind = 0;"
    stilgar_dist="      update bodies set metal = metal + ($m_stilgar_res * 3),     crystal = crystal + ($m_stilgar_res * 2),     deuterium = deuterium + ($m_stilgar_res * 1)      where user_id = 11 and kind = 0;"
    gunslinger_dist="   update bodies set metal = metal + ($m_gunslinger_res * 3),  crystal = crystal + ($m_gunslinger_res * 2),  deuterium = deuterium + ($m_gunslinger_res * 1)   where user_id = 13 and kind = 0;"
    pistolpete_dist="   update bodies set metal = metal + ($m_pistolpete_res * 3),  crystal = crystal + ($m_pistolpete_res * 2),  deuterium = deuterium + ($m_pistolpete_res * 1)   where user_id = 33 and kind = 0;"
    cuzzer_dist="       update bodies set metal = metal + ($m_cuzzer_res * 3),      crystal = crystal + ($m_cuzzer_res * 2),      deuterium = deuterium + ($m_cuzzer_res * 1)       where user_id = 28 and kind = 0;"
    psql -d retro-game -U postgres -t -c "$grim_dist"; echo "Updating Resources!" # Grim
    psql -d retro-game -U postgres -t -c "$jorj_dist"; echo "Updating Resources!" # Jorj
    psql -d retro-game -U postgres -t -c "$stilgar_dist"; echo "Updating Resources!" # Stilgar
    psql -d retro-game -U postgres -t -c "$gunslinger_dist"; echo "Updating Resources!" # Gunslinger
    psql -d retro-game -U postgres -t -c "$pistolpete_dist"; echo "Updating Resources!" # PistolPete
    psql -d retro-game -U postgres -t -c "$cuzzer_dist"; echo "Updating Resources!" # Cuzzer
    echo "Sleeping 1 Min"; sleep 60
done