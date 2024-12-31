package com.nhnacademy.hello.common.config;

import com.nhnacademy.hello.common.security.OAuth2.hanlder.CustomOAuth2LoginSuccessHandler;
//import com.nhnacademy.hello.common.security.OAuth2.resolver.CustomOAuth2AuthorizationRequestResolver;
import com.nhnacademy.hello.common.security.OAuth2.service.CustomOAuth2UserService;
import com.nhnacademy.hello.common.filter.JwtAuthenticationFilter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final JwtUtils jwtUtils;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 토큰 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // jwt 토큰 기반 인증을 하기 때문에 세션 비활성화
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 페이지 권한 설정
        http.authorizeHttpRequests(
                authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/","/oauth/**", "/login/**", "/oauth2/**").permitAll()
                                .anyRequest().permitAll()
        );

//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers("/","/oauth/**", "/login/**", "/oauth2/**").permitAll() // Oauth2 URL 접근 허용
//                .anyRequest().authenticated()  // 그 외 모든 요청 인증 필요
//        );


        // 로그인 설정
        http.formLogin(
                        formLogin ->
                                formLogin.loginPage("/login")
        );

        // Oauth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(customOAuth2UserService)
                )
//                .authorizationEndpoint(auth ->
//                        auth.authorizationRequestResolver(new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository))
//                )
                .successHandler(customOAuth2LoginSuccessHandler)
        );



        // 로그아웃 설정
        http.logout(
                logout ->
                        logout.logoutUrl("/logout")
                                // 로그아웃 시 홈페이지로 이동
                                .logoutSuccessUrl("/?clearLocalCart=true")
                                .deleteCookies("token")
        );

        // 필터 설정
        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtProperties, jwtUtils),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
