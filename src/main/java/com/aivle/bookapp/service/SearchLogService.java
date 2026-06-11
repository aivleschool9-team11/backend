package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.SearchLog;
import com.aivle.bookapp.domain.SearchResultClick;
import com.aivle.bookapp.repository.SearchLogRepository;
import com.aivle.bookapp.repository.SearchResultClickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 검색 로그와 클릭 관련 Service
 */

@Service
@RequiredArgsConstructor
public class SearchLogService {
    private final SearchLogRepository searchLogRepository;
    private final SearchResultClickRepository searchResultClickRepository;

    /**
     * 1. SearchLog 저장
     */
    @Transactional
    public SearchLog saveSearchLog(SearchLog searchLog) {
        return searchLogRepository.save(searchLog);
    }

    /**
     * 2. SearchLog 전체 조회
     */
    @Transactional(readOnly = true)
    public List<SearchLog> findAll() {
        return searchLogRepository.findAll();
    }

    /**
     * 3. SearchResultClick 저장
     */
    @Transactional
    public SearchResultClick saveClickLog(SearchResultClick click) {
        return searchResultClickRepository.save(click);
    }

    /**
     * 4. SearchLog의 ID를 통해 관련 SearchResultClick 조회
     */
    @Transactional(readOnly = true)
    public List<SearchResultClick> findClickBySearchLogId(Long id) {
        return searchResultClickRepository.findBySearchLogId(id);

    }
}
