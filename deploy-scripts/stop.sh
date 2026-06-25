#!/bin/bash

PID=$(pgrep -f ".jar")

if [ -n "$PID" ]; then
  echo "Stopping application PID: $PID"
  kill -15 $PID
  sleep 5
else
  echo "Application not running"
fi