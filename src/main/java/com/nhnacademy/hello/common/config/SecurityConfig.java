package com.nhnacademy.hello.common.config;

import com.nhnacademy.hello.common.filter.JwtAuthenticationFilter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 토큰 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // jwt 토큰 기반 인증을 하기 때문에 세션 비활성화
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 페이지 권한 설정
        http.authorizeHttpRequests(
                authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll()
        );

        // 로그인 설정
        http.formLogin(
                        formLogin ->
                                formLogin.loginPage("/login")
        );


        // 로그아웃 설정
        http.logout(
                logout ->
                        logout.logoutUrl("/logout")
                                // 로그아웃 시 홈페이지로 이동
                                .logoutSuccessUrl("/")
                                .deleteCookies("token")
        );

        // 필터 설정
        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtProperties, jwtUtils),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
