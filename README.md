# 📚 AIVLE Book Manager — Backend

**AIVLE Book Manager의 백엔드 서버**입니다. 도서 정보의 영속화, 키워드·의미(시맨틱) 검색, 태그 관리, 검색·클릭 로그 수집 등 React 프론트엔드가 사용하는 REST API를 제공합니다.

프론트엔드 미니프로젝트 단계에서 임시로 쓰던 가상 백엔드(JSON-Server / `db.json`)를 **Spring Boot + JPA + H2 기반의 실제 백엔드로 대체**하고, 여기에 **AI 표지 저장 API까지 확장**했습니다. 생성형 AI 결과물(표지·요약·해시태그·임베딩)은 프론트엔드에서 OpenAI를 직접 호출해 생성하며 이를 저장·조회·검색하는 역할을 담당합니다.

> 🔗 프론트엔드 레포: https://github.com/aivleschool9-team9/frontend

---

## 🌟 핵심 기능

### 1. 도서 정보 CRUD + 통합 검색·정렬
- 도서 등록 / 부분 수정(PATCH) / 삭제 / 단건·목록 조회
- 키워드(제목·저자) 검색 + **5종 정렬**(최신순·오래된순·제목순·작가명순·좋아요순) + 태그 필터링
- 좋아요 동기화(프론트 LocalStorage 찜 상태 ↔ 백엔드 전체 카운트)

### 2. AI 크리에이티브 콘텐츠 저장
- 프론트가 OpenAI로 생성한 **요약·감성 광고 카피·추천 해시태그·표지 이미지**를 받아 영속화
- **AI가 생성한 해시태그**는 `BookTag ↔ Tag`로 정규화 저장 → 태그 클릭 시 도서 필터(`GET /books?tag=`)

### 3. AI 의미(시맨틱) 검색
- 프론트가 OpenAI Embedding으로 변환한 쿼리 벡터를 받아 **코사인 유사도** 연산 → 자연어 의미 기반 도서 추천

### 4. ⭐ 검색·클릭 행동 로그 — Outcome(성과) 관점 설계
- 본 프로젝트의 차별점으로, 강의에서 다룬 **Output → Outcome 관점**을 서비스 설계에 반영했습니다.
- 기능의 구현 여부를 넘어, 그 기능이 실제 사용자 행동으로 이어지는지를 측정하기 위해 모든 검색(`SearchLog`)과 그 결과 클릭(`SearchResultClick`)을 기록하고, 응답의 `searchLogId`로 둘을 연결합니다.
- 노출 순위(`rankPosition`)·유사도(`similarityScore`)까지 함께 수집하여, **CTR(클릭률)·랭킹 품질·키워드 vs AI 의미 검색 성과 비교** 등 서비스가 실제로 잘 작동하는지를 정량적으로 분석할 수 있습니다.

### 5. 견고한 예외 처리
- 사용자 정의 예외(`BookNotFoundException` 등) + 전역 예외 처리(`@RestControllerAdvice`)
- 404(없음) / 400(검증 실패) / 500(서버 오류) 일관된 응답 정제

---

## 🛠 기술 스택

| 구분 | 기술 |
| --- | --- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.0.6 (Web MVC, Data JPA, Validation) |
| **ORM / DB** | Spring Data JPA (Hibernate) / H2 (In-Memory) |
| **Build** | Gradle |
| **Library** | Lombok |
| **AI 연동** | OpenAI (프론트엔드 직접 호출 — GPT Image, `gpt-4o-mini`, Embedding) |

---

## 🏗 시스템 아키텍처

**기본 흐름 (CRUD)**
```
[React (5173/3000)] ──GET/POST/PATCH/DELETE──▶ [Spring Boot (8080)] ──JPA──▶ [H2 Database]
                                                (Controller-Service-Repository)
```

**AI 흐름 (표지 자동 생성)**
```
① React → OpenAI(GPT Image)  : POST + prompt
② OpenAI → React             : b64_json 응답
③ React: b64_json → Blob 변환
④ React → Spring Boot        : POST /books/upload-image (multipart) → 서버 저장 후 URL 반환
⑤ React → Spring Boot        : PATCH /books/{id}/cover (반환받은 URL만 저장)
```
> 프론트 미니프로젝트의 json-server를 **Spring Boot + JPA + H2로 대체**하고, **AI 표지 저장 API까지 확장**한 구조입니다.

**레이어 구조 (관심사 분리 + DTO)**
```
Controller ──(Request/Response DTO)──▶ Service ──▶ Repository ──▶ Entity
  ├ BookController        요청: BookCreateRequest 등    ├ BookService
  ├ TagController         응답: BookResponse 등          ├ TagService
  ├ SearchController     (Service에서 Entity ↔ DTO 변환) ├ BookEmbeddingService
  └ SearchLogController                                 └ SearchLogService
```
> 클라이언트와는 **DTO로만 주고받고**(엔티티 직접 노출 X), 엔티티는 순수 영속 모델로 유지합니다. 서버 관리 필드(`id`·`createdAt`·`likes` 등)의 외부 주입을 차단하고, 목록 응답을 경량화합니다.

---

## 🗂 도메인 모델 (ERD)
<img width="980" height="526" alt="ERD" src="https://github.com/user-attachments/assets/9c18e817-2bb9-4fb4-87e4-79f4ca2a4466" />

| 엔티티 | 주요 필드 | 설명 |
| --- | --- | --- |
| **Book** | id, title, author, content, summary, copy, coverImageUrl, likes, createdAt, updatedAt | 도서 (검증 어노테이션 적용) |
| **Tag** | id, name(unique) | 태그 |
| **BookTag** | id, bookId, tagId | 도서-태그 M:N 조인 테이블 |
| **BookEmbedding** | id, bookId, embeddingJson, embeddingModel, embeddingDurationMs, embeddingUpdatedAt | 도서 임베딩 벡터(JSON 문자열) |
| **SearchLog** | id, searchType, query, matchedBookCount, durationMs, searchedAt | 검색 로그 |
| **SearchResultClick** | id, searchLogId, bookId, rankPosition, similarityScore, clickedAt | 검색 결과 클릭 로그 |

> 엔티티는 **순수 영속 필드만** 가지며, 태그·임베딩 등 입출력 데이터는 **DTO**가 담당합니다. 태그는 `BookTag`, 임베딩 벡터는 `BookEmbedding` 테이블에 정규화하여 저장합니다.

---

## 🏃 실행 방법

```bash
# 요구 사항: JDK 17
./gradlew bootRun        # 서버 실행 (http://localhost:8080)
./gradlew build          # 빌드
```
- H2 콘솔: `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:bookdb`, User `sa`, PW 없음
- DB는 인메모리(`ddl-auto: create`)라 **재시작 시 초기화**됩니다.
- CORS는 `localhost:5173`, `localhost:3000` 허용.

---

## 📡 API 명세

> Base URL: `http://localhost:8080`

### 📖 도서 (Book)
| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/books` | 도서 목록 (검색·정렬·태그 필터: `?keyword=&sort=&tag=`) |
| `GET` | `/books/{id}` | 도서 상세 조회 |
| `POST` | `/books` | 도서 등록 (입력 검증) |
| `PATCH` | `/books/{id}` | 도서 부분 수정 |
| `DELETE` | `/books/{id}` | 도서 삭제 |
| `PATCH` | `/books/{id}/likes` | 좋아요 수 설정 |
| `PATCH` | `/books/{id}/tags` | 태그 수정 |
| `PATCH` | `/books/{id}/cover` | AI 표지(Base64) 저장 |
| `PATCH` | `/books/{id}/embedding` | 임베딩 저장/백필 |
| `POST` | `/books/upload-image` | 표지 이미지 업로드(multipart) → 저장 URL 반환 |

> 업로드된 이미지는 서버 `./uploads` 에 저장되고, `GET /uploads/{파일명}` 정적 리소스 핸들러로 서빙됩니다.

### 🏷 태그 (Tag)
| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/tags` | 전체 태그 목록 조회 |

### 🔍 검색 (Search)
| Method | Endpoint | Body | 설명 |
| --- | --- | --- | --- |
| `POST` | `/search` | `{ query, sort, tag }` | 키워드/태그/정렬 검색 + 검색 로그 저장 |
| `POST` | `/search/semantic` | `{ query, queryVector, topK }` | AI 의미 검색 (코사인 유사도) + 로그 저장 |
| `POST` | `/search/{searchLogId}/click` | `{ bookId, rankPosition, similarityScore }` | 검색 결과 클릭 로그 저장 |

### 📊 로그 (Log)
| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/books/search-log` | 전체 검색 로그 조회 |
| `GET` | `/books/search-log/{searchId}` | 특정 검색 로그의 클릭 로그 조회 |

### 응답 코드
`200/201/204` 성공 · `400` 검증 실패 · `404` 리소스 없음 · `500` 서버 오류

### 📦 요청/응답 모델 (DTO)
모든 입출력은 엔티티가 아닌 DTO로 처리합니다.

| 구분 | DTO | 필드 |
| --- | --- | --- |
| 등록 요청 | `BookCreateRequest` | title, author, content, summary, copy, coverImageUrl, **tags**(List), **embeddingJson**(List\<Float>), embeddingDurationMs |
| 수정 요청 | `BookUpdateRequest` | 위 필드 전부 `Optional<>` (보낸 필드만 반영하는 **부분 수정**) |
| 좋아요 요청 | `BookLikesRequest` | likes |
| 상세/등록/수정 응답 | `BookResponse` | id, title, author, content, summary, copy, coverImageUrl, likes, createdAt, updatedAt, **tags** |
| 목록/검색 응답 | `BookSummaryResponse` | id, title, author, coverImageUrl, likes, **similarityScore**(시맨틱 검색 시에만) |

---

## 📂 프로젝트 구조

```
src/main/java/com/aivle/bookapp
├── controller/   # REST 엔드포인트 (Book, Tag, Search, SearchLog)
├── dto/          # 요청/응답 DTO
│   ├── request/  #   BookCreateRequest, BookUpdateRequest, BookLikesRequest
│   └── response/ #   BookResponse, BookSummaryResponse
├── service/      # 비즈니스 로직 (Book, Tag, BookEmbedding, SearchLog)
├── repository/   # Spring Data JPA 레포지토리
├── domain/       # 엔티티 (Book, Tag, BookTag, BookEmbedding, SearchLog, SearchResultClick)
├── exception/    # 사용자 정의 예외 + GlobalExceptionHandler
└── config/       # WebConfig (CORS)
```

---

## 📅 미션별 진행 내역

| 일차 | 미션 | 주요 내용 |
| --- | --- | --- |
| **1일차** | 미션 1·2 | 기획/설계(ERD·API 정의서·R&R), 환경설정 + 전 계층 골격, WebConfig(CORS) |
| **2일차** | 미션 3·4 | Repository·Service·`GET /books`·`GET /books/{id}`, POST/PATCH/DELETE + 입력 검증, 풀스택 CRUD |
| **3일차** | 미션 5·6 | 사용자 정의 예외 + `@Transactional`, 전역 예외 처리(`@RestControllerAdvice`, 404/400 정제) |
| **4일차** | 미션 7·8 | AI 표지 저장 흐름, 태그·임베딩·시맨틱 검색·클릭 로그 고도화, E2E 시연 및 발표 |

---

## 🔍 구현 세부 내용 (주요 소스코드)

> 미션 3~7의 핵심 구현과, 미션을 넘어 추가로 구현한 심화 기능을 정리합니다.

### 📌 미션 3 — Repository + Service + 조회(GET 2종)
`JpaRepository` 기반 Repository, 생성자 주입(`@RequiredArgsConstructor`) Service, 목록·상세 조회 엔드포인트.
```java
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);
}

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }
}
// BookController: GET /books (목록·검색·정렬), GET /books/{id} (상세)
```

### 📌 미션 4 — 등록/수정/삭제 + 입력 검증
엔티티 검증 어노테이션 + `@PrePersist`(생성 시각·기본값 자동화) + POST/PATCH/DELETE.
```java
@Entity @Table(name = "books")
public class Book {
    @NotBlank(message = "도서 제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.likes == null) this.likes = 0; // @Builder.Default 미적용(역직렬화) 경로 방어
    }
}
// BookController: POST(@Valid 검증) / PATCH(부분 수정) / DELETE
```

### 📌 미션 5 — 사용자 정의 예외 + `@Transactional`
조회 실패 시 사용자 정의 예외 발생, CUD는 쓰기·조회는 읽기 전용 트랜잭션 적용.
```java
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) { super("Book not found: id=" + id); }
}

@Service @RequiredArgsConstructor
public class BookService {
    @Transactional(readOnly = true)              // 조회: 읽기 전용
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }
    @Transactional                               // CUD: 쓰기 트랜잭션
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) throw new BookNotFoundException(id);
        bookRepository.deleteById(id);
    }
}
```

### 📌 미션 6 — 전역 예외 처리 (`@RestControllerAdvice`)
모든 컨트롤러의 예외를 한 곳에서 일관된 형식(404/400/500)으로 정제.
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)          // 404
    public ResponseEntity<Map<String,String>> handleNotFound(BookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Book not found", "message", e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) // 400 (검증 실패)
    public ResponseEntity<Map<String,String>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Validation failed", "message", msg));
    }
    @ExceptionHandler(Exception.class)                       // 500
    public ResponseEntity<Map<String,String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Server error", "message", "서버 내부 오류가 발생했습니다."));
    }
}
```

### 📌 미션 7 — AI 표지 생성 흐름
프론트가 OpenAI로 표지를 생성하면, 백엔드는 이미지를 파일로 업로드받아 저장하고(`POST /books/upload-image`) 그 URL을 도서에 연결(`PATCH /books/{id}/cover`)합니다.
```java
@PatchMapping("/{id}/cover")
public ResponseEntity<BookResponse> updateBookCover(@PathVariable Long id, ...) {
    Book updated = bookService.updateCover(id, coverImageUrl);
    return ResponseEntity.ok(BookResponse.fromBookAndTags(updated, tags));
}

public Book updateCover(Long id, String coverImageUrl) {
    Book existing = findById(id);
    existing.setCoverImageUrl(coverImageUrl);
    return bookRepository.save(existing);
}
// 흐름: React → OpenAI(GPT Image) → React(Blob) → POST /books/upload-image(파일 저장·URL 반환) → PATCH /books/{id}/cover(URL 저장)
```

---

### ➕ 추가 구현 기능 (심화 — 미션 외)

미션 요구사항을 넘어 서비스 완성도를 위해 추가로 구현한 기능들입니다.

#### ① 통합 검색·정렬 & 좋아요
키워드(제목·저자) 검색 + **5종 정렬**(최신순·오래된순·제목순·작가명순·좋아요순) + 태그 필터를 한 메서드에서 처리. 좋아요는 프론트 LocalStorage 찜 상태와 백엔드 전체 카운트를 동기화.
```java
public List<Book> findAllWithFilter(String keyword, String sort, String tag) {
    List<Book> result = (tag != null && !tag.isEmpty())  ? findByTagName(tag)
        : (keyword != null && !keyword.isBlank())        ? bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword)
        : bookRepository.findAll();
    if ("likes".equals(sort))  result.sort((a, b) -> b.getLikes() - a.getLikes());
    else if ("newest".equals(sort)) result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    // oldest / title / author ...
    return result;
}
```

#### ② DTO 도입 — 부분 수정(Optional) & 정적 팩토리
엔티티 직접 노출을 막고, 요청·응답을 DTO로 분리. 수정은 `Optional`로 진짜 부분 수정 지원.
```java
public class BookUpdateRequest {                 // 보낸 필드만 반영
    @Builder.Default private Optional<String> title = Optional.empty();
    public boolean hasTitle() { return title != null && title.isPresent(); }
}
public class BookResponse {                      // 정적 팩토리로 Entity → DTO
    public static BookResponse fromBookAndTags(Book book, List<String> tags) { /* ... */ }
}
public class BookSummaryResponse {               // 목록 경량 응답 (+시맨틱 유사도)
    @JsonInclude(JsonInclude.Include.NON_NULL) private Double similarityScore;
}
```

#### ③ AI 크리에이티브 콘텐츠 — 요약·광고 카피·해시태그·표지
프론트가 OpenAI로 도서별 **핵심 요약(`summary`)·감성 광고 카피(`copy`)·추천 해시태그(`tags`)·표지 이미지(`coverImageUrl`)** 를 실시간 생성하면, 백엔드가 이를 받아 영속화합니다. 특히 **AI가 생성한 해시태그**는 `BookTag ↔ Tag`로 **정규화 저장**하여, 태그 클릭 시 도서 필터(`GET /books?tag=`)로 재활용합니다.
```java
// AI가 생성한 해시태그 → BookTag 테이블에 정규화 저장
if (tags != null && !tags.isEmpty()) tagService.saveBookTags(saved.getId(), tags);

// 조회 시: BookTag → Tag 를 조립해 응답에 태그를 채움
List<String> tagNames = bookTagRepository.findByBookId(book.getId()).stream()
        .map(bt -> tagRepository.findById(bt.getTagId()).orElse(null))
        .filter(Objects::nonNull).map(Tag::getName).collect(Collectors.toList());
```

#### ④ AI 의미 검색 — 임베딩 + 코사인 유사도
"우울할 때 위로가 되는 책"처럼 문장형 자연어로 검색하면, 프론트가 OpenAI Embedding으로 변환한 쿼리 벡터와 저장된 도서 임베딩의 **코사인 유사도**를 계산해 의미가 가까운 도서를 정렬합니다.
```java
// 임베딩 벡터(List<Float>) → JSON 문자열로 저장 후, 쿼리 벡터와 코사인 유사도 계산
private double cosineSimilarity(float[] a, float[] b) {
    double dot = 0, normA = 0, normB = 0;
    for (int i = 0; i < a.length; i++) { dot += a[i]*b[i]; normA += a[i]*a[i]; normB += b[i]*b[i]; }
    return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
}
```

#### ⑤ 검색·클릭 행동 로그 — Outcome(성과) 관점 설계

**설계 의도.** 기능의 *구현 여부(Output)* 만 확인하는 데 그치지 않고, 강의에서 다룬 **Outcome 관점**에 따라 *그 기능이 실제 사용자 행동으로 이어지는지* 까지 데이터로 확인할 수 있도록 설계했습니다. 이를 위해 모든 검색과 그 결과에 대한 클릭을 기록하고, 두 로그를 연결하는 구조를 구성했습니다.

**구현.**
- 검색 로그(`SearchLog`): 검색 1건마다 `searchType`(KEYWORD/SEMANTIC) · `query` · `matchedBookCount` · `durationMs` · `searchedAt` 저장
- 클릭 로그(`SearchResultClick`): 결과 클릭 시 `searchLogId` · `bookId` · `rankPosition`(노출 순위) · `similarityScore`(유사도) · `clickedAt` 저장
- 검색 응답에 `searchLogId`를 포함하여, 이후 발생한 클릭을 해당 검색과 연결

**측정 가능한 지표.** 수집된 로그로 다음을 분석할 수 있습니다.

| 지표 | 산식 / 근거 필드 | 확인 내용 |
| --- | --- | --- |
| CTR(클릭률) | 클릭 수 / 검색 수 | 검색 결과가 실제 클릭으로 이어진 비율 |
| 순위별 클릭 분포 | `rankPosition` | 상위 노출 도서의 실제 클릭 집중도 (랭킹 품질) |
| 키워드 vs 시맨틱 CTR | `searchType` 별 CTR | AI 의미 검색과 키워드 검색의 효과 비교 |
| 유사도–클릭 상관 | `similarityScore` × 클릭 여부 | 유사도 점수와 실제 클릭의 상관 (추천 신뢰도) |

**의의.** 이 구조를 통해 *"기능이 동작한다"* 는 확인을 넘어, *"AI 검색·추천이 사용자의 클릭(행동)으로 이어졌는지"* 를 정량적으로 검증할 수 있습니다.

---

## 🐛 트러블슈팅

### ⭐ AI 커버 이미지 저장 실패 (DB / Critical)
- **증상**: AI로 생성한 표지를 저장(`PATCH /books/{id}/cover`)하면 저장이 실패함.
- **원인**: 프론트가 OpenAI 이미지(`b64_json`)를 **Base64 Data URL**(수십~수백 KB의 매우 긴 문자열)로 변환해 **JSON 본문에 그대로 담아** 전송 → DB `coverImageUrl` 컬럼(`VARCHAR(255)`) 한계를 초과해 `Data too long` 발생.
- **해결**: 거대한 Base64 문자열을 DB에 저장하는 대신, **이미지를 파일로 업로드하고 DB에는 짧은 접근 URL만 저장**하도록 구조를 변경.
  - **프론트**: Base64 → `Blob` 변환 후 `multipart/form-data`로 전송
  - **백엔드**: 업로드 엔드포인트(`POST /books/upload-image`)에서 파일을 서버 `./uploads`에 저장하고 접근 URL 반환, 정적 리소스 핸들러(`/uploads/**`)로 서빙
  - 이후 그 **짧은 URL**을 `PATCH /books/{id}/cover`로 저장 → `VARCHAR(255)` 한계 문제를 근본적으로 회피
  ```java
  // Backend: 표지 이미지 업로드 (파일 저장 → URL 반환)
  @PostMapping("/upload-image")
  public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
      Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();   // 절대경로
      Files.createDirectories(uploadDir);
      String fileName = UUID.randomUUID() + ".png";
      Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
      return ResponseEntity.ok(Map.of("url", "http://localhost:8080/uploads/" + fileName));
  }
  ```
- **회고**: "Base64 인코딩 = 매우 긴 문자열"이라는 데이터 특성을 간과한 사례. **대용량 미디어는 텍스트 컬럼에 문자열로 저장하기보다, 파일로 업로드하고 URL만 저장**하는 것이 적절하다는 점을 학습.

### 📁 업로드 파일 저장 경로 오류 (상대경로 → Tomcat 임시 디렉토리)
- **증상**: `POST /books/upload-image` 시 `500`. `java.io.FileNotFoundException: ...\work\Tomcat\...\uploads\xxx.png (지정된 경로를 찾을 수 없습니다)`
- **원인**: 저장 경로를 `new File("./uploads/...")` 처럼 **상대경로**로 두면, 실행 환경에 따라 작업 디렉토리가 프로젝트 루트가 아닌 **Tomcat 임시 폴더**로 잡힘 → 그 위치에 `uploads` 디렉토리가 없고, `MultipartFile.transferTo()`가 디렉토리 자동 생성 없이 실패.
- **해결**:
  1. 저장 경로를 **절대경로**로 변환 — `Paths.get("uploads").toAbsolutePath().normalize()`
  2. `transferTo()` 대신 **`Files.copy(file.getInputStream(), …, REPLACE_EXISTING)`** 사용
  3. 정적 리소스 핸들러(`/uploads/**`)도 **동일 절대경로**를 가리키도록 일치 → 업로드 후 이미지 정상 표시
- **회고**: 파일 I/O는 실행 환경마다 달라지는 **상대경로 의존을 피하고 절대경로로 명시**해야 안정적이며, 저장 경로와 서빙 경로를 같은 기준으로 일치시켜야 함.

### 기타 해결한 이슈
| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 도서 등록 시 500 | `@Builder.Default`가 역직렬화 경로에 미적용 → `likes` null | `@PrePersist`에서 null 방어 |
| PATCH 부분 수정 불가(400) | 엔티티 `@Valid`가 필수 필드 강제 | 수정 요청을 `Optional` 기반 `BookUpdateRequest`로 분리(보낸 필드만 반영) |
| 좋아요 수 폭증 | 프론트는 '새 총합' 전송, 백엔드는 누적 | `setLikes` 설정 방식으로 변경 |
| 태그 저장/조회 안 됨 | 정규화(BookTag) 미연결 + 응답 조립 부재 | 요청 DTO로 태그 수신 + 응답 DTO에 `BookTag→Tag` 조립 |
| 임베딩 없는 책 삭제 시 404 | 정리용 삭제가 없을 때 예외 throw | idempotent 삭제로 변경 |
| 검색 클릭 저장 500 | `Map<String,Object>` 숫자 캐스팅 실패 | `((Number)v).longValue()` 안전 변환 |

---

## 📦 산출물
- 소스코드(`AI_##조.zip`), 결과보고서(`AI_##조.pptx`), `README.md` 포함 압축 제출
