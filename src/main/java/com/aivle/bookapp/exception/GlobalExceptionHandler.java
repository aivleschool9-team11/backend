package com.aivle.bookapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — 도서를 찾을 수 없음
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BookNotFoundException e) {
        Map<String, String> body = Map.of("error", "Book not found", "message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 404 — 임베딩을 찾을 수 없음
    @ExceptionHandler(BookEmbeddingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BookEmbeddingNotFoundException e) {
        Map<String, String> body = Map.of("error", "Book Embedding not found", "message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 400 — @Valid 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        Map<String, String> body = Map.of("error", "Validation failed", "message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 — 잘못된 JSON 형식 또는 데이터 타입 오류
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        Map<String, String> body = Map.of(
            "error", "Bad Request",
            "message", "요청 데이터 형식이 올바르지 않습니다. 필드 타입을 확인해주세요."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 500 — 그 외 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> body = Map.of("error", "Server error", "message", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}