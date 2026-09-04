#!/usr/bin/env bash

echo "Stop the Backend container"
docker stop maestro-ui-backend-1
sleep 5s

echo "Remove the Backend container"
docker rm maestro-ui-backend-1 
sleep 5s

echo "Remove the Backend image"
docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'ui-backend')
sleep 5s

echo "Download Backend image"
docker compose pull ui-backend
sleep 5s

echo "Start the Backend container"
docker compose up -d

