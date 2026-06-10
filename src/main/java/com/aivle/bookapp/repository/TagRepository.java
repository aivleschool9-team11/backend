package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long>{
    // 태그명으로 태그 조회 (중복 체크용)
    Tag findByName(String name);
}
