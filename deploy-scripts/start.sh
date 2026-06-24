#!/bin/bash

cd /home/ubuntu/app

# Parameter Store에서 값 꺼내기
DB_URL=$(aws ssm get-parameter \
  --name "/bookapp/db-url" \
  --with-decryption \
  --query "Parameter.Value" \
  --output text \
  --region ap-northeast-2)

DB_USERNAME=$(aws ssm get-parameter \
  --name "/bookapp/db-username" \
  --with-decryption \
  --query "Parameter.Value" \
  --output text \
  --region ap-northeast-2)

DB_PASSWORD=$(aws ssm get-parameter \
  --name "/bookapp/db-password" \
  --with-decryption \
  --query "Parameter.Value" \
  --output text \
  --region ap-northeast-2)

# 값 주입해서 실행
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url=${DB_URL} \
  -Dspring.datasource.username=${DB_USERNAME} \
  -Dspring.datasource.password=${DB_PASSWORD} \
  *.jar \
  > /home/ubuntu/app/app.log 2>&1 &

echo "Spring Boot started"