# 📖 BookApp Backend
### AI 기반 도서 추천 및 관리 서비스

Spring Boot와 JPA를 기반으로 구축된 **AI 의미(Semantic) 검색 및 도서 관리 시스템**의 백엔드 저장소입니다. 생성형 AI 모델링 데이터(태그, 홍보 카피, 표지, 임베딩 벡터)를 효율적으로 적재하고, 무결성이 보장된 다대다 구조와 실시간 검색 트래킹 로그 시스템을 제공합니다.

---

## 🛠️ 기술 스택 및 개발 환경 (Tech Stack)

- **Language**: Java 17
- **Framework**: Spring Boot 4.0.6 (Gradle)
- **Database**: H2 Database (In-Memory)
- **ORM / JPA**: Spring Data JPA / Hibernate
- **Libraries**: Lombok, Jakarta Validation, Jackson

---

## 🗄️ 데이터베이스 아키텍처 (ERD & 구조)

단순 문자열 저장을 통한 데이터 왜곡과 쿼리 성능 저하를 방지하기 위해, 엔티티 간의 관계를 완벽히 정규화한 **3-Table 다대다(N:M) 매핑 아키텍처**를 구축했습니다.

- **books (도서 테이블)**: 도서 본연의 메타데이터 관리
- **tags (태그 마스터 테이블)**: 시스템 내 전체 태그 종류의 유니크성 보장 (`unique = true`)
- **book_tags (매핑 테이블)**: 도서 ID와 태그 ID를 매핑하여 무결성 유지
- **book_embeddings (임베딩 테이블)**: AI 시맨틱 검색을 위한 1536차원 벡터 데이터 독립 관리
- **search_logs & search_result_clicks**: 사용자 검색 패턴 추적 및 추천 품질 개선을 위한 로그 저장소

---

## 🔌 API 엔드포인트 명세서 (API Specification)

### 📚 도서 자원 관리 (Book Controller)

| 기능 | Method | URL | Request Body | 사용 페이지 및 주요 기능 | Status |
|:---:|:---:|:---|:---|:---|:---:|
| **목록 조회** | **GET** | `/books` | **없음** | 도서 목록 페이지 (전체 조회, 렌더링) | 200 |
| **상세 조회** | **GET** | `/books/{id}` | **없음** | 도서 상세/수정 페이지 (단건 조회) | 200, 404 |
| **도서 등록** | **POST** | `/books` | `{ title, author, summary, content, copy, tags, coverImageUrl, embeddingJson, embeddingDurationMs }` | 도서 등록 페이지 (새 도서 + AI 이미지 URL 저장) | 201 |
| **정보 수정** | **PATCH** | `/books/{id}` | `{ title, author, summary, content, coverImageUrl, embeddingJson, embeddingDurationMs }` | 도서 수정 페이지 (변경된 필드만 부분 수정) | 200, 404 |
| **도서 삭제** | **DELETE** | `/books/{id}` | **없음** | 도서 상세/수정 페이지 (데이터 완전 삭제) | 204, 404 |
| **좋아요 설정** | **PATCH** | `/books/{id}/likes` | `{ "likes": 5 }` | 도서 상세/수정 페이지 (좋아요 수 증감 반영) | 200, 404 |
| **표지 업데이트** | **PATCH** | `/books/{id}/cover` | `{ "coverImageUrl": "..." }` | AI 표지 저장 기능 (생성된 이미지를 기존 도서에 저장) | 200, 404 |
| **태그 수정** | **PATCH** | `/books/{id}/tags` | `{ "tags": ["판타지", "모험"] }` | 특정 도서의 태그 전면 교체 및 재매핑 | 200, 404 |

### 🔍 검색 및 AI 로그 제어 (Search Controller)

| 기능 | Method | URL | Request Body | 설명 | Status |
|:---:|:---:|:---|:---|:---|:---:|
| **통합 검색** | **POST** | `/search` | `{ "query": "...", "sort": "...", "tag": "..." }` | 키워드/태그/정렬 통합 검색 + 로그 저장 | 200 |
| **의미 검색** | **POST** | `/search/semantic` | `{ "queryVector": [...], "topK": 5 }` | 1536차원 벡터 기반 AI 시맨틱 검색 | 200 |
| **클릭 로그** | **POST** | `/search/{logId}/click` | `{ "bookId": 1, "rankPosition": 1, "similarityScore": 0.9 }` | 검색 결과 클릭 추적 및 품질 개선 로그 생성 | 200 |

### 🌐 외부 AI 인터페이스 (OpenAI API Integration)

| 서비스 | Method | Endpoint | 주요 역할 및 연동 시점 | Status |
|:---:|:---:|:---|:---|:---:|
| **이미지 생성** | **POST** | `.../v1/images/generations` | 프롬프트 송신 후 생성된 이미지를 `coverImageUrl`로 전달 | 200 |
| **텍스트 분석** | **POST** | `.../v1/chat/completions` | 도서 요약, 카피, 태그 생성 및 검색어 확장(Expanded Query) | 200 |
| **임베딩 변환** | **POST** | `.../v1/embeddings` | 텍스트의 1536차원 벡터화 (도서 등록 및 시맨틱 검색 시 활용) | 200 |

---

## 📂 프로젝트 구조 (Directory Structure)
```text
src/main/java/com/aivle/bookapp/
├── config/          # CORS 및 보안 관련 설정 (WebConfig)
├── controller/      # API 엔드포인트 매핑 (Book, Tag, Search Controller)
├── domain/          # JPA 엔티티 정의 (Book, Tag, BookTag, SearchLog 등)
├── exception/       # 비즈니스 예외 처리 및 글로벌 예외 핸들러
├── repository/      # Spring Data JPA 레포지토리 인터페이스
└── service/         # 핵심 비즈니스 로직 및 외부 데이터 파싱 가공 레이어
```


