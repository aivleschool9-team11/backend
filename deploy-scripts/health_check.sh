#!/bin/bash

sleep 10

RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

if [ "$RESPONSE" = "200" ]; then
  echo "Health check passed"
  exit 0
else
  echo "Health check failed: $RESPONSE"
  exit 1
fi