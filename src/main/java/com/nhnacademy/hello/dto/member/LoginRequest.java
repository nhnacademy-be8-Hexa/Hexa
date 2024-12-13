package com.nhnacademy.hello.dto.member;

public record LoginRequest(
        String id,
        String password
) {
}
