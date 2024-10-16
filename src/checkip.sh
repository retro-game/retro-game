#!/bin/bash
# Get the container id for the game service container
    #container_id=$(docker ps -q --filter name=*retro*)
# Get the list of unique IP addresses
    #ips=$(docker logs "$container_id" | grep -oE "ip=[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+" | sort | uniq -d)
ips=$(docker logs 5c155b58603b | grep -oE "ip=[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+" | sort | uniq -d)


# Loop through each IP address and run a grep command
for ip in $ips
do
  echo "Searching for logs with IP address $ip ..."
  docker logs 5c155b58603b | grep "$ip" | sort | uniq
done
