#!/bin/bash

# BeforeInstall 단계에서 실행 (Install 직전, 현재 배포의 스크립트로 동작).
# 기존/실패 배포가 남긴 jar를 제거해 Install 단계의
# "file already exists" 충돌을 방지한다.
rm -rf /home/ubuntu/app/*.jar
echo "기존 jar 정리 완료"
exit 0
