package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long>{
    // 특정 도서의 태그 연결 목록 조회
    List<BookTag> findByBookId(Long bookId);

    // 특정 태그에 연결된 도서 목록 조회
    List<BookTag> findByTagId(Long tagId);

    // 특정 도서의 태그 연결 전체 삭제
    void deleteByBookId(Long bookId);
}
