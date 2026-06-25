#!/bin/bash

# jar 파일 삭제
rm -f /home/ubuntu/app/*.jar
echo "jar 파일 삭제 완료"

# deploy-scripts 삭제 (sudo 추가)
sudo rm -rf /home/ubuntu/deploy-scripts/
echo "deploy-scripts 삭제 완료"

exit 0