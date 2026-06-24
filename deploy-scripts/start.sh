#!/bin/bash

cd /home/ubuntu/app

nohup java -jar \
  -Dspring.profiles.active=prod \
  *.jar \
  > /home/ubuntu/app/app.log 2>&1 &

echo "Spring Boot started"