package com.nhnacademy.hello.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter

// 프로피티 에서 관련 설정 가져옴
public class JwtProperties {
    private String accessSecret;
    private String refreshSecret;
    private Integer accessTokenExpirationTime;
    private Integer refreshTokenExpirationTime;
    private String tokenPrefix;
    private String headerString;

}
