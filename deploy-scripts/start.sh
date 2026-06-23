#!/bin/bash

cd /home/ec2-user/app

nohup java -jar \
  -Dspring.profiles.active=prod \
  *.jar \
  > /home/ec2-user/app/app.log 2>&1 &

echo "Spring Boot started"