#!/bin/bash

cd /home/ubuntu/app

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

nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url=${DB_URL} \
  -Dspring.datasource.username=${DB_USERNAME} \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Dcloud.aws.region.static=ap-northeast-2 \
  -Dcloud.aws.s3.bucket=team11-s3-001 \
  *.jar \
  > /home/ubuntu/app/app.log 2>&1 &

echo "Spring Boot started"