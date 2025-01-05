package com.nhnacademy.hello.dto.jwt;

public record AccessRefreshTokenDTO(
         String accessToken,
         String refreshToken
) {
}
