package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.SearchLog;
import com.aivle.bookapp.domain.SearchResultClick;
import com.aivle.bookapp.dto.response.BookSummaryResponse;
import com.aivle.bookapp.service.BookService;
import com.aivle.bookapp.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 검색 관련 REST API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final BookService bookService;
    private final SearchLogService searchLogService;

    /**
     * 1. 통합 검색 (POST /search)
     * - body: { "query": "검색어", "sort": "newest", "tag": "태그" }
     * - 키워드/태그/정렬 검색 실행 + 검색 로그 자동 저장
     * - 모든 파라미터 선택값, 없으면 전체 조회
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        String sort = body.get("sort");
        String tag = body.get("tag");

        log.info("Request to search - query: {}, sort: {}, tag: {}", query, sort, tag);

        long startTime = System.currentTimeMillis();
        List<BookSummaryResponse> books = bookService.findAllWithFilter(query, sort, tag);
        long durationMs = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("books", books);

        // 검색어 있을 때만 로그 저장
        if (query != null && !query.isBlank()) {
            SearchLog searchLog = new SearchLog();
            searchLog.setSearchType("KEYWORD");
            searchLog.setQuery(query);
            searchLog.setMatchedBookCount(books.size());
            searchLog.setDurationMs(durationMs);
            searchLog.setSearchedAt(LocalDateTime.now());
            SearchLog saved = searchLogService.saveSearchLog(searchLog);
            response.put("searchLogId", saved.getId());
        } else {
            response.put("searchLogId", null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 2. AI 의미 검색 (POST /search/semantic)
     * - body: { "queryVector": [...], "topK": 5 }
     * - React에서 OpenAI로 변환한 검색어 벡터를 전달
     * - Spring Boot에서 코사인 유사도 계산 후 유사 도서 반환
     * - 검색 로그 자동 저장
     */
    // TODO: DTO 도입 시 SemanticSearchRequest로 교체 예정
    // (현재 Map<String, Object> 사용으로 unchecked 경고 발생)
    @PostMapping("/semantic")
    public ResponseEntity<Map<String, Object>> semanticSearch(@RequestBody Map<String, Object> body) {
        List<Double> vectorList = (List<Double>) body.get("queryVector");
        int topK = body.containsKey("topK") ? (int) body.get("topK") : 5;

        float[] queryVector = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
            queryVector[i] = vectorList.get(i).floatValue();
        }

        log.info("Request to semantic search - topK: {}", topK);

        long startTime = System.currentTimeMillis();
        List<BookSummaryResponse> books = bookService.semanticSearch(queryVector, null, topK);
        long durationMs = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("books", books);

        // 검색어 있을 때만 로그 저장
        String query = body.containsKey("query") ? (String) body.get("query") : null;
        if (query != null && !query.isBlank()) {
            SearchLog searchLog = new SearchLog();
            searchLog.setSearchType("SEMANTIC");
            searchLog.setQuery(query);
            searchLog.setMatchedBookCount(books.size());
            searchLog.setDurationMs(durationMs);
            searchLog.setSearchedAt(LocalDateTime.now());
            SearchLog saved = searchLogService.saveSearchLog(searchLog);
            response.put("searchLogId", saved.getId());
        } else {
            response.put("searchLogId", null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 3. 검색 결과 클릭 로그 저장 (POST /search/{searchLogId}/click)
     * body: { "bookId": 1, "rankPosition": 1, "similarityScore": 0.9 }
     * AI 의미 검색 시에만 저장, 클릭 시 순위/유사도 기록
     */
    @PostMapping("{searchLogId}/click")
    public void saveClickLog(@PathVariable Long searchLogId, @RequestBody Map<String, Object> body) {
        Long bookId = ((Number) body.get("bookId")).longValue();
        Integer rankPosition = ((Number) body.get("rankPosition")).intValue();
        Float similarityScore = ((Number) body.get("similarityScore")).floatValue();
        LocalDateTime time = LocalDateTime.now();

        SearchResultClick click = SearchResultClick.builder().searchLogId(searchLogId).bookId(bookId).rankPosition(rankPosition).similarityScore(similarityScore).clickedAt(time).build();

        searchLogService.saveClickLog(click);
    }
}
