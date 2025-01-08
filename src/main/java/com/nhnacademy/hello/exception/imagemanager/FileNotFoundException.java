package com.nhnacademy.hello.exception.imagemanager;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
