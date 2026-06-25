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

export DB_URL
export DB_USERNAME
export DB_PASSWORD
export DDL_AUTO=update
export S3_BUCKET=team11-s3-001

nohup java -jar \
  *.jar \
  > /home/ubuntu/app/app.log 2>&1 &

echo "Spring Boot started"