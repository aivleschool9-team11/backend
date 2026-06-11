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
        // TODO: 태그/임베딩은 요청 DTO 스펙 확정 후 연결 (현재는 도서 기본 정보만 저장)
        Book savedBook = bookService.create(book, null, null, null);

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
        // TODO: 태그/임베딩은 요청 DTO 스펙 확정 후 연결 (현재는 도서 기본 정보만 수정)
        Book updatedBook = bookService.update(id, book, null, null, null);
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
    public ResponseEntity<Book> updateLikes(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int likes = body.getOrDefault("likes", 0);
        log.info("Request to set likes for book id: {}, likes: {}", id, likes);
        Book updatedBook = bookService.updateLikes(id, likes);
        return ResponseEntity.ok(updatedBook);
    }
}