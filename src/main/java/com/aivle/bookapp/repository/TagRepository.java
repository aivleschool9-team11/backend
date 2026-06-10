package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long>{
}
