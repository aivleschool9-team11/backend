package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Tag;
import com.aivle.bookapp.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 태그 관련 REST API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 전체 태그 목록 조회 (GET /tags)
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        log.info("Request to get all tags");
        return ResponseEntity.ok(tagService.findAll());
    }
}
