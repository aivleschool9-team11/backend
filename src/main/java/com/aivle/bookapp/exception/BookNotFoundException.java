package com.aivle.bookapp.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book not found: id=" + id);
    }
}