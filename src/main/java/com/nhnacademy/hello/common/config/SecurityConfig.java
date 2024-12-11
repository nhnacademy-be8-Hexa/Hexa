package com.nhnacademy.hello.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf().disable()

                .formLogin()
                .loginPage("/login") // 커스텀 로그인 페이지
                .loginProcessingUrl("/login/process") // 로그인 요청 처리 URL
                .permitAll()

                .and()

                .authorizeRequests()
                .antMatchers("/login", "/signup").permitAll() // 로그인, 회원가입 허용
                .anyRequest().authenticated(); // 나머지는 인증 필요

//                .and()

//                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }

}
