package com.nhnacademy.hello.common.security.OAuth2.hanlder;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.security.OAuth2.service.PaycoUserService;
import com.nhnacademy.hello.dto.member.LoginRequest;
import com.nhnacademy.hello.dto.member.MemberRequestDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {


    private final JwtProperties jwtProperties;
    private final MemberAdapter memberAdapter;

    @Lazy
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private PaycoUserService paycoUserService;

    @Value("${jwt_token_cookie_secure}")
    private String secure;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if(authentication instanceof OAuth2AuthenticationToken){

            // 권한에서 유저 추출
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            // 유저 정보에서 등급 추출
            String role = oauth2User.getAuthorities().stream().findFirst().get().getAuthority();
            log.error("role: {}", role);
            // 나머지 정보도 추출
            Map<String, Object> attributes = oauth2User.getAttributes();

            log.error("service: {}", (((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().equals("payco")));
            //페이코 이고
            if(((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().equals("payco")) {
                // 유저 정보가 빠진게 있으면
                Boolean checkUserInfo = checkUserInfoBlank(attributes);
                if (!checkUserInfo) {

                    // 클라이언트 등록 ID 를 가져오고
                    String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

                    OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                            clientRegistrationId, oauthToken.getName()
                    );

                    String clientId = authorizedClient.getClientRegistration().getClientId();
                    log.error("clientId: {}", clientId);

                    // 접근 토큰을 가져와서
                    String accessToken = authorizedClient.getAccessToken().getTokenValue();
                    log.error("accessToken: {}", accessToken);


                    // 이 2개로 페이코 회원 가입한거 무효화 시키고
                    paycoUserService.revokeOfferAgreement(clientId, accessToken);

                    authorizedClientService.removeAuthorizedClient(clientRegistrationId, oauthToken.getName());
                    request.getSession().invalidate();
                    SecurityContextHolder.clearContext();

                    request.setAttribute("error", "페이코 로그인 시 개인정보를 모두 동의 해주셔야 하고, 페이코도 인증이 된 상태여야 합니다.");
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/login");
                    dispatcher.forward(request, response);
                    return;

                }

            }

            String token;

            // Oauth2의 아아디와 패스워드는 제공 서버의 이름과 그 제공서버의 아이디를 조합하여 지정
            String Oauth2IdAndPwd = attributes.get("provider")+"_"+attributes.get("providerId");
            log.error("Oauth2IdAndPwd: {}", Oauth2IdAndPwd);
            token = memberAdapter.login(new LoginRequest(Oauth2IdAndPwd, Oauth2IdAndPwd));

            // 만약에 토큰이 없으면 (즉 회원이 없으면) 회원 가입 하고 다시 토큰 발급
            if(token == null  && checkUserInfoBlank(attributes)){

                LocalDate localDate = LocalDate.now();
                if(((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().equals("payco"))
                {
                    // 회원 가입하고 다시 토큰
                    int month = Integer.parseInt(attributes.get("birthday").toString().substring(0, 2));
                    int day = Integer.parseInt(attributes.get("birthday").toString().substring(2, 4));
                    localDate = LocalDate.of(0, month , day);
                }
                else {
                    int year = Integer.parseInt(attributes.get("birthday").toString().substring(0, 4));
                    int month = Integer.parseInt(attributes.get("birthday").toString().substring(4, 6));
                    int day = Integer.parseInt(attributes.get("birthday").toString().substring(6, 8));
                    localDate = LocalDate.of(year, month , day);
                }

                log.error("localDate: {}", localDate);
                MemberRequestDTO memberRequestDTO = new MemberRequestDTO(
                        Oauth2IdAndPwd,
                        passwordEncoder.encode(Oauth2IdAndPwd),
                        attributes.get("name").toString() ,
                        attributes.get("phone").toString() ,
                        attributes.get("email").toString() ,
                        localDate,
                        null,
                        "1",
                        "1"

                );
                memberAdapter.createMember(memberRequestDTO);
                log.error("memberCreate:{}", memberRequestDTO.toString());
                token = memberAdapter.login(new LoginRequest(Oauth2IdAndPwd, Oauth2IdAndPwd));
            }

            log.error("token: {}", token);

            // 토큰을 쿠키에 저장한다
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(jwtProperties.getExpirationTime());
            cookie.setSecure(Boolean.parseBoolean(secure));
            response.addCookie(cookie);

            // 로그인 시간 업데이트 요청을 보낸다
            memberAdapter.loginMember(Oauth2IdAndPwd);

        }

        response.sendRedirect("/");
    }

    private boolean checkUserInfoBlank(Map<String, Object> attributes){
        // 하나라도 비어있으면 false 다 있으면 true
        return
                !((String)attributes.get("birthday")).isBlank() &&
                        !((String)attributes.get("phone")).isBlank() &&
                        !((String)attributes.get("provider")).isBlank()&&
                        !((String)attributes.get("providerId")).isBlank() &&
                        !((String)attributes.get("name")).isBlank() &&
                        !((String)attributes.get("email")).isBlank();
    }

}
