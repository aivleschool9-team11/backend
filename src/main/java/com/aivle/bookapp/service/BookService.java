package com.aivle.bookapp.service;


import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.Tag;
import com.aivle.bookapp.dto.request.BookCreateRequest;
import com.aivle.bookapp.dto.request.BookUpdateRequest;
import com.aivle.bookapp.dto.response.BookResponse;
import com.aivle.bookapp.dto.response.BookSummaryResponse;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.BookTagRepository;
import com.aivle.bookapp.repository.TagRepository;

import java.util.Arrays;
import java.util.Objects;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagService tagService;
    private final BookTagRepository bookTagRepository;
    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;

    // Book을 BookResponse로 변환
    public BookResponse makeBookResponse(Book book) {
        return populateTags(book);
    }

    // List<Book>를 목록 조회용 DTO로 변환
    public List<BookSummaryResponse> makeBookSummaryResponseList(List<Book> books) {
        return books.stream()
                .map(BookSummaryResponse::from)
                .collect(Collectors.toList());
    }

    // 전체 도서 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findAll(){
        List<Book> books = bookRepository.findAll();
        books.forEach(this::populateTags);
        return books;
    }

    // 특정 도서 단건 조회
    @Transactional(readOnly = true)
    public Book findById(Long id){
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return book;
    }

    // 도서 객체에 태그 리스트 채우기 (BookTag → Tag 조립, 응답용)
    private BookResponse populateTags(Book book) {
        List<String> tagNames = bookTagRepository.findByBookId(book.getId()).stream()
                .map(bt -> tagRepository.findById(bt.getTagId()).orElse(null))
                .filter(Objects::nonNull)
                .map(Tag::getName)
                .collect(Collectors.toList());
        return BookResponse.fromBookAndTags(book, tagNames);
    }

    // 태그만 별도 수정 (PATCH /books/{id}/tags)
    @Transactional
    public BookResponse updateTags(Long id, List<String> tags) {
        Book existing = findById(id);
        tagService.deleteByBookId(id);
        if (tags != null && !tags.isEmpty()) {
            tagService.saveBookTags(id, tags);
        }
        populateTags(existing);
        return makeBookResponse(existing);
    }

    // 새 도서 등록 + 태그 저장 + 임베딩 저장
    @Transactional
    public BookResponse create(BookCreateRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .content(request.getContent())
                .summary(request.getSummary())
                .copy(request.getCopy())
                .coverImageUrl(request.getCoverImageUrl())
                .build();
        Book saved = bookRepository.save(book);

        if (request.getEmbeddingJson() != null && !request.getEmbeddingJson().isEmpty()) {
            String vectorStr = toVectorString(request.getEmbeddingJson());
            jdbcTemplate.update(
                    "UPDATE books SET embedding = ?::vector WHERE id = ?",
                    vectorStr, saved.getId()
            );
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagService.saveBookTags(saved.getId(), request.getTags());
        }

        return makeBookResponse(saved);
    }


    // 도서 부분 수정
    @Transactional
    public BookResponse update(Long id, BookUpdateRequest request) {
        Book existing = findById(id);

        if (request.hasTitle()) existing.setTitle(request.getTitle());
        if (request.hasAuthor()) existing.setAuthor(request.getAuthor());
        if (request.hasSummary()) existing.setSummary(request.getSummary());
        if (request.hasContent()) existing.setContent(request.getContent());
        if (request.hasCopy()) existing.setCopy(request.getCopy());
        if (request.hasCoverImageUrl()) existing.setCoverImageUrl(request.getCoverImageUrl());

        if (request.hasEmbeddingJson()) {
            String vectorStr = toVectorString(request.getEmbeddingJson());
            jdbcTemplate.update(
                    "UPDATE books SET embedding = ?::vector WHERE id = ?",
                    vectorStr, existing.getId()
            );
        }

        Book updated = bookRepository.save(existing);

        if (request.hasTags()) {
            List<String> tags = request.getTags();
            tagService.deleteByBookId(id);
            if (!tags.isEmpty()) {
                tagService.saveBookTags(id, tags);
            }
        }

        return makeBookResponse(updated);
    }

    // 도서 삭제
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        tagService.deleteByBookId(id);
        bookRepository.deleteById(id);
    }

    // 좋아요 수 설정 (프론트가 계산한 새 총합을 그대로 반영)
    @Transactional
    public BookResponse updateLikes(Long id, int likes) {
        Book existing = findById(id);
        existing.setLikes(Math.max(0, likes));
        Book saved = bookRepository.save(existing);
        return makeBookResponse(saved);
    }

    // AI 표지 저장
    @Transactional
    public BookResponse updateCover(Long id, String coverImageUrl) {
        Book existing = findById(id);
        existing.setCoverImageUrl(coverImageUrl);
        Book saved = bookRepository.save(existing);
        return makeBookResponse(saved);
    }

    // 임베딩 백필
    @Transactional
    public BookResponse updateEmbedding(Long id, List<Float> values) {
        Book existing = findById(id);
        String vectorStr = toVectorString(values);
        jdbcTemplate.update(
                "UPDATE books SET embedding = ?::vector WHERE id = ?",
                vectorStr, id
        );
        return makeBookResponse(existing);
    }

    // 특정 태그에 속한 도서 목록 조회
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> findByTagName(String tagName) {
        return makeBookSummaryResponseList(findBooksByTagName(tagName));
    }

    private List<Book> findBooksByTagName(String tagName) {
        return tagService.findByName(tagName).stream()
                .flatMap(tag -> bookTagRepository.findByTagId(tag.getId()).stream())
                .map(bookTag -> findById(bookTag.getBookId()))
                .collect(Collectors.toList());
    }

    // 키워드 검색 + 정렬
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> findAllWithFilter(String keyword, String sort, String tag){
        List<Book> books;
        if (tag != null && !tag.isEmpty()) {
            books = findBooksByTagName(tag);
        } else if (keyword != null && !keyword.isBlank()) {
            books = bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword);
        } else {
            books = bookRepository.findAll();
        }

        if ("newest".equals(sort)) books.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        else if ("oldest".equals(sort)) books.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        else if ("title".equals(sort)) books.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
        else if ("author".equals(sort)) books.sort((a, b) -> a.getAuthor().compareTo(b.getAuthor()));
        else if ("likes".equals(sort)) books.sort((a, b) -> (b.getLikes() == null ? 0 : b.getLikes()) - (a.getLikes() == null ? 0 : a.getLikes()));

        // searchLogService.saveSearchLog() 호출
        return makeBookSummaryResponseList(books);
    }

    // AI 의미 검색 + 코사인 유사도 계산
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> semanticSearch(float[] queryVector, int topK){
        String vectorStr = Arrays.toString(queryVector);

        List<Book> books = bookRepository.findSimilarBooks(vectorStr, topK);

        return books.stream()
                .map(book -> {
                    Double score = bookRepository.findSimilarityScore(vectorStr, book.getId());
                    return BookSummaryResponse.from(book, score);
                })
                .collect(Collectors.toList());
    }


    // List<Float> -> float [] 변환 유틸
    private float[] toFloatArray(List<Float> values){
        float [] arr = new float[values.size()];
        for(int i =0; i < values.size(); i++){
            arr[i] = values.get(i);
        }
        return arr;
    }

    // float[] → "[0.1, 0.2, ...]" String 변환
    private String toVectorString(List<Float> values) {
        return values.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
