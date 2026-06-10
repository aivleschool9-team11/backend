package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.BookTag;
import com.aivle.bookapp.domain.Tag;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.BookTagRepository;
import com.aivle.bookapp.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;
    private final BookRepository bookRepository;

    // 태그 전체 조회
    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    // 도서ID로 태그 조회
    @Transactional(readOnly = true)
    public List<BookTag> findByBookId(Long bookId) {
        // 도서 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }
        return bookTagRepository.findByBookId(bookId);
    }

    // 태그 저장
    @Transactional
    public Tag save(String name) {
        // 중복 태그 체크
        Tag existing = tagRepository.findByName(name);
        if (existing != null) {
            return existing;
        }
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    // 도서에 태그 목록 연결 저장
    @Transactional
    public void saveBookTags(Long bookId, List<String> tagNames) {
        for (String tagName : tagNames) {
            Tag tag = save(tagName);
            BookTag bookTag = new BookTag();
            bookTag.setBookId(bookId);
            bookTag.setTagId(tag.getId());
            bookTagRepository.save(bookTag);
        }
    }

    // 도서의 태그 연결 삭제
    @Transactional
    public void deleteByBookId(Long bookId) {
        bookTagRepository.deleteByBookId(bookId);
    }

    // 태그 이름으로 태그 조회
    @Transactional(readOnly = true)
    public List<Tag> findByName(String tagName) {
        Tag tag = tagRepository.findByName(tagName);
        return tag != null ? List.of(tag) : List.of();
    }

}