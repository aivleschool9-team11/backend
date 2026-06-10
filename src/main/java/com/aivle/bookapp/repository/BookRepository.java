package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 도서 레포지토리 (JPA)
 */
public interface BookRepository extends JpaRepository<Book, Long> {
    // 제목 또는 저자 포함 검색
    List<Book> findByTitleContainingOrAuthorContaining(String titleKeyword, String authorKeyword);
    
    // 태그 포함 검색 (단순 문자열 매칭)
    List<Book> findByTagsContaining(String tagName);
}
