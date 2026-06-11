package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String tag) {

        log.info("Request to get books - keyword: {}, sort: {}, tag: {}", keyword, sort, tag);

        List<Book> books;

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
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        log.info("Request to get book by id: {}", id);
        Book book = bookService.findById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * 3. 신규 도서 등록 (POST /books)
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        log.info("Request to create book: {}", book.getTitle());
        // 태그 목록을 추출하여 전달
        Book savedBook = bookService.create(book, book.getTags(), null, null);

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
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        log.info("Request to update book id: {}", id);
        // 태그 목록을 추출하여 전달
        Book updatedBook = bookService.update(id, book, book.getTags(), null, null);
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
     * 6. 좋아요 수 증가/감소 (PATCH /books/{id}/likes)
     * - body: { "likes": 1 } 또는 { "likes": -1 }
     * - 유저별 좋아요 상태는 프론트 localStorage 관리, 백엔드는 전체 카운트만 증감
     */
    @PatchMapping("/{id}/likes")
    public ResponseEntity<Book> updateLikes(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int likes = body.getOrDefault("likes", 0);
        log.info("Request to update likes for book id: {}, delta: {}", id, likes);
        Book updatedBook = bookService.updateLikes(id, likes);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 7. 도서 태그 수정 (PATCH /books/{id}/tags)
     * 태그 정보만 별도로 수정하는 기능
     */
    @PatchMapping("/{id}/tags")
    public ResponseEntity<Book> updateTags(@PathVariable Long id, @RequestBody Book bookRequest) {
        log.info("Request to update tags for book id: {}", id);
        Book updatedBook = bookService.updateTags(id, bookRequest.getTags());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 8. AI 표지 저장 기능(PATCH /books/{id}/cover)
     * 변경된 표지 이미지만 URL만 부분 수정
     */
    @PatchMapping("/{id}/cover")
    public ResponseEntity<Book> updateBookCover(
            @PathVariable Long id,
            @RequestBody Book bookRequest){
        log.info("Request to update book cover for book id: {}, coverImageUrl: {}", id, bookRequest.getCoverImageUrl());

        Book updateBook = bookService.updateCover(id, bookRequest.getCoverImageUrl());
        return ResponseEntity.ok(updateBook);
    }

    /**
     * 9. 도서 임베딩 백필(PATCH /books/{id}/embedding)
     * 임베딩 JSON 데이터와 소요 시간 저장
     *
     */
    @PatchMapping("/{id}/embedding")
    public ResponseEntity<Book> updateBookEmbedding(
            @PathVariable Long id,
            @RequestBody Book bookRequest){
        log.info("Request to update embedding for id: {}", id);

        Book updateBook = bookService.updateEmbedding(
                id,
                bookRequest.getEmbeddingJson(),
                bookRequest.getEmbeddingDurationMs()
        );
        return ResponseEntity.ok(updateBook);
    }
}