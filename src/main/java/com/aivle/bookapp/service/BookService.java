package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 도서 비즈니스 로직 처리 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    // 전체 도서 목록 조회
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * 키워드 검색 + 정렬 + 태그 필터링 (통합 검색)
     */
    public List<Book> findAllWithFilter(String keyword, String sort, String tag) {
        List<Book> result = (keyword == null || keyword.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword);

        if ("newest".equals(sort)) result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        else if ("oldest".equals(sort)) result.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        else if ("title".equals(sort)) result.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
        else if ("likes".equals(sort)) result.sort((a, b) -> (b.getLikes() == null ? 0 : b.getLikes()) - (a.getLikes() == null ? 0 : a.getLikes()));

        return result;
    }

    /**
     * 태그로 도서 검색
     */
    public List<Book> findByTagName(String tagName) {
        log.info("Searching books by tag: {}", tagName);
        return bookRepository.findByTagsContaining(tagName);
    }

    /**
     * ID로 도서 상세 조회
     */
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    /**
     * 새 도서 등록 + 태그 저장 + 임베딩 저장
     */
    @Transactional
    public Book create(Book book, List<String> tags, String embeddingJson, Long embeddingDurationMs) {
        if (book.getLikes() == null) {
            book.setLikes(0);
        }
        Book saved = bookRepository.save(book);

        // 태그/임베딩 저장 로직 (필요 시 추가)
        return saved;
    }

    /**
     * 도서 정보 수정 (AI 연동 규격 반영)
     */
    @Transactional
    public Book update(Long id, Book book, List<String> tags, String embeddingJson, Long embeddingDurationMS) {
        Book existing = findById(id);

        if (book.getTitle() != null) existing.setTitle(book.getTitle());
        if (book.getAuthor() != null) existing.setAuthor(book.getAuthor());
        if (book.getIsbn() != null) existing.setIsbn(book.getIsbn());
        if (book.getSummary() != null) existing.setSummary(book.getSummary());
        if (book.getContent() != null) existing.setContent(book.getContent());
        if (book.getCopy() != null) existing.setCopy(book.getCopy());
        if (book.getCoverImageUrl() != null) existing.setCoverImageUrl(book.getCoverImageUrl());
        if (book.getLikes() != null) existing.setLikes(book.getLikes());
        if (book.getTags() != null) existing.setTags(book.getTags());

        return bookRepository.save(existing);
    }

    /**
     * 도서 삭제
     */
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    // 좋아요 업데이트
    @Transactional
    public Book updateLikes(Long id, int likes) {
        Book existing = findById(id);
        int current = existing.getLikes() == null ? 0 : existing.getLikes();
        existing.setLikes(current + likes);
        return bookRepository.save(existing);
    }
}
