#!/bin/bash
p_user_id=3
p_hw_id=3
p_astro=11

m_prod=$((12000 * $p_astro))
c_prod=$((8000 * $p_astro))
d_prod=$((4000 * $p_astro))

# loop until stopped
while true; do
  econ="update flights set metal = metal + $m_prod, crystal = crystal + $c_prod, deuterium = deuterium + $d_prod where id = 4242 and start_user_id = $p_user_id;"
  psql -d retro-game -U postgres -t -c "$econ"; echo "Updating Resources!"
  echo "Sleeping 1 Min"; sleep 60
done