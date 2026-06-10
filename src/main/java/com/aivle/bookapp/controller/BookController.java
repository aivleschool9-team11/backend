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
            books = bookService.findAll(keyword, sort);
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
        Book savedBook = bookService.save(book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedBook);
    }

    /**
     * 4. 도서 정보 수정 (PATCH /books/{id})
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        log.info("Request to update book id: {}", id);
        Book updatedBook = bookService.update(id, book);
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
}