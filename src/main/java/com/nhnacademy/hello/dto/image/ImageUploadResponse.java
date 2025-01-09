package com.nhnacademy.hello.dto.image;

import lombok.Getter;
import lombok.Setter;

public class ImageUploadResponse {
    @Getter
    @Setter
    private String url;

    public ImageUploadResponse() {}

    public ImageUploadResponse(String url) {
        this.url = url;
    }
}
