package com.aivle.bookapp.exception;

public class BookEmbeddingNotFoundException extends RuntimeException {
    public BookEmbeddingNotFoundException(Long bookId) {
        super("Embedding not found : book id=" + bookId);
    }
}
