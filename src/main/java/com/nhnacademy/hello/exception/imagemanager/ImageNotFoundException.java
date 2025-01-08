package com.nhnacademy.hello.exception.imagemanager;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
