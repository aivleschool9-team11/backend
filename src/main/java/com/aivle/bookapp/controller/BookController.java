package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.dto.request.BookCreateRequest;
import com.aivle.bookapp.dto.request.BookLikesRequest;
import com.aivle.bookapp.dto.request.BookUpdateRequest;
import com.aivle.bookapp.dto.response.BookResponse;
import com.aivle.bookapp.dto.response.BookSummaryResponse;
import com.aivle.bookapp.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 도서 관련 REST API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 1. 도서 목록 조회 (통합 검색, 정렬, 태그 필터링 포함)
     * - GET /books
     * - GET /books?keyword=어린왕자&sort=newest
     * - GET /books?tag=스프링
     */
    @GetMapping
    public ResponseEntity<List<BookSummaryResponse>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String tag) {

        log.info("Request to get books - keyword: {}, sort: {}, tag: {}", keyword, sort, tag);

        List<BookSummaryResponse> books;

        // 태그별 도서 조건 처리
        if (tag != null && !tag.isEmpty()) {
            books = bookService.findByTagName(tag);
        } else {
            // 일반 목록 조회 및 키워드 검색 (정렬 조건 포함)
            books = bookService.findAllWithFilter(keyword, sort, null);
        }

        return ResponseEntity.ok(books);
    }

    /**
     * 2. 특정 도서 상세 조회 (GET /books/{id})
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        log.info("Request to get book by id: {}", id);
        Book book = bookService.findById(id);
        BookResponse bookResponse = bookService.makeBookResponse(book);

        return ResponseEntity.ok(bookResponse);
    }

    /**
     * 3. 신규 도서 등록 (POST /books)
     */
    @PostMapping
    @Transactional
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest bookRequest) {
        log.info("Request to create book: {}", bookRequest.getTitle());
        // 요청 body의 tags / embedding 을 그대로 전달 (Book 의 @Transient 필드)
        BookResponse savedBook = bookService.create(bookRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedBook);
    }

    /**
     * 4. 도서 정보 수정 (PATCH /books/{id})
     * 부분 수정이므로 @Valid 를 적용하지 않는다 (필수 필드 일부만 보내도 허용).
     * 실제 부분 반영은 BookService.update 의 null 체크가 담당.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest bookRequest) {
        log.info("Request to update book id: {}", id);
        BookResponse updatedBook = bookService.update(id, bookRequest);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 5. 특정 도서 삭제 (DELETE /books/{id})
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Request to delete book id: {}", id);
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 6. 좋아요 수 설정 (PATCH /books/{id}/likes)
     * - body: { "likes": 5 } — 프론트가 계산한 '새 좋아요 총합'을 그대로 받아 설정
     * - 유저별 좋아요 상태는 프론트 localStorage 관리
     */
    @PatchMapping("/{id}/likes")
    public ResponseEntity<BookResponse> updateLikes(
            @PathVariable Long id,
            @Valid @RequestBody BookLikesRequest request) {
        log.info("Request to set likes for book id: {}, likes: {}", id, request.getLikes());
        BookResponse updatedBook = bookService.updateLikes(id, request.getLikes());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 7. 도서 태그 수정 (PATCH /books/{id}/tags)
     * - body: { "tags": ["판타지", "모험"] }
     */
    @PatchMapping("/{id}/tags")
    public ResponseEntity<BookResponse> updateTags(
            @PathVariable Long id,
            @RequestBody BookUpdateRequest bookRequest) {
        log.info("Request to update tags for book id: {}", id);
        BookResponse updatedBook = bookService.updateTags(id, bookRequest.getTags());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 8. AI 표지 저장 (PATCH /books/{id}/cover)
     * - body: { "coverImageUrl": "https://..." }
     */
    @PatchMapping("/{id}/cover")
    public ResponseEntity<BookResponse> updateBookCover(
            @PathVariable Long id,
            @RequestBody BookUpdateRequest bookRequest) {
        log.info("Request to update cover for book id: {}", id);
        BookResponse updatedBook = bookService.updateCover(id, bookRequest.getCoverImageUrl());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 9. 도서 임베딩 백필 (PATCH /books/{id}/embedding)
     * - body: { "embeddingJson": [0.1, 0.2], "embeddingDurationMs": 123 }
     */
    @PatchMapping("/{id}/embedding")
    public ResponseEntity<BookResponse> updateBookEmbedding(@PathVariable Long id, @RequestBody BookUpdateRequest bookRequest) {
        log.info("Request to update embedding for book id: {}", id);
        BookResponse updatedBook = bookService.updateEmbedding(id, bookRequest.getEmbeddingJson(), bookRequest.getEmbeddingDurationMs());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 10. 표지 이미지 업로드 (POST /books/upload-image)
     * - multipart/form-data: { "file": 이미지 파일 }
     * - 절대 경로 기반으로 /uploads 폴더에 저장 후 접근 가능한 URL 반환
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID() + ".png";
        Path filePath  = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Image uploaded: {}", fileName);
        String url = "http://localhost:8080/uploads/" + fileName;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
