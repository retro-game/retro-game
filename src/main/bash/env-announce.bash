#!/bin/bash
webhook_url="https://discord.com/api/webhooks/1096872402195062994/VL_v9Wfs6iDmIL-sRsd-RbSviA7RXISDsGMxO4syUQwF6FWkORHOLAqFXzZQoKYW4Qhf"
input3="With the alien presence deminished, societies from all over the galaxy have once again begun to send envoys to win your favor"
# Send MEssage
curl -H "Content-Type: application/json" -X POST -d '{"content": "'"$input3"'"}' $webhook_url