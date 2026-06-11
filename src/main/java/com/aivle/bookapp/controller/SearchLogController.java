package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.SearchLog;
import com.aivle.bookapp.domain.SearchResultClick;
import com.aivle.bookapp.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 검색 로그 관련 REST API 요청을 처리하는 컨트롤러입니다.
 * 기본적으로 조회 기능만 구현됐으며, 저장은 검색 관련 컨트롤러에서 수행합니다.
 */

@Slf4j
@RestController
@RequestMapping("/books/search-log")
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogService searchLogService;


    /**
     * 1. 검색 로그 전체 조회 (GET /books/search-log)
     */
    @GetMapping
    public ResponseEntity<List<SearchLog>> getAllSearchLog() {
        log.info("Request to get all search logs");
        return ResponseEntity.ok(searchLogService.findAll());
    }

    /**
     *  2. 특정 로그 관련 클릭 로그 조회 GET/books/search-log/{searchID}
     */
    @GetMapping("/{searchId}")
    public ResponseEntity<List<SearchResultClick>> getSearchResultClickBySearchLogId(@PathVariable Long searchId) {
        log.info("Request to get search result click by search log id : {}", searchId);
        return ResponseEntity.ok(searchLogService.findClickBySearchLogId(searchId));
    }

}
