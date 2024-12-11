package com.nhnacademy.hello.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 토큰 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // 페이지 권한 설정
        http.authorizeHttpRequests(
                authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/login/**").permitAll()
                                .requestMatchers("/signup").permitAll()
                                .requestMatchers("/", "/index.html").permitAll()
                                .anyRequest().authenticated()
        );

        // 로그인 설정
        http.formLogin(
                        formLogin ->
                                formLogin.loginPage("/login")
        );

        return http.build();
    }

}
