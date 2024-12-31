package com.nhnacademy.hello.common.security.OAuth2.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {

        // 기본 OAuth2AuthorizationRequestResolver 에서 요청을 풀어서
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);

        if (authorizationRequest == null) {
            log.error("authorizationRequest == null");
            return null;
        }

        // 거기서 인증 서버 대상이 payco 확인
        String registrationId = authorizationRequest.getAttribute("registration_id");
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        String redirectUri = authorizationRequest.getRedirectUri();


        // 만약 페이코면 관련 파라미터 값 추가해주고 아니면 그냥 그대로 싸서
        String customAuthorizationUri = null;
       if(registrationId.equals("payco")){
           customAuthorizationUri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
                   .queryParam("serviceProviderCode", "FRIENDS")
                   .queryParam("userLocale", "ko_KR")
                   .build().toUriString();

       }
       else {
            customAuthorizationUri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
                   .build().toUriString();
       }
        log.error("customAuthorizationUri: {}", customAuthorizationUri);
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientRegistration.getClientId())
                .redirectUri(redirectUri)
                .authorizationUri(customAuthorizationUri)
                .scopes(authorizationRequest.getScopes())
                .state(authorizationRequest.getState())
                .attributes(authorizationRequest.getAttributes())
                .build();


    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        log.error("OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) error");
        return null;
    }

}
