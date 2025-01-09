package com.nhnacademy.hello.dto.tag;

public record TagDTO(
        Long tagId,
        String tagName
) {

    public Long getTagId() {
        return tagId;
    }

    public String getTagName() {
        return tagName;
    }
}
