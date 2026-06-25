#!/bin/bash

PID=$(pgrep -f ".jar")

if [ -n "$PID" ]; then
  echo "Stopping application PID: $PID"
  kill -15 $PID
  sleep 5
else
  echo "Application not running"
fi

# jar 파일 삭제
rm -f /home/ubuntu/app/*.jar
echo "jar 파일 삭제 완료"