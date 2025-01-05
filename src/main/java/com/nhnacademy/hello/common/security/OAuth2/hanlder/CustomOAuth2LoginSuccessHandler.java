package com.nhnacademy.hello.common.security.OAuth2.hanlder;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.auth.TokenAdapter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.security.OAuth2.service.PaycoUserService;
import com.nhnacademy.hello.common.util.CookieUtil;
import com.nhnacademy.hello.common.util.JwtUtils;
import com.nhnacademy.hello.dto.jwt.AccessRefreshTokenDTO;
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
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {


    private final JwtProperties jwtProperties;
    private final TokenAdapter tokenAdapter;
    private final MemberAdapter memberAdapter;
    private final JwtUtils jwtUtils;

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

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String role = getUserRole(oauth2User);

            log.debug("Authenticated user role: {}", role);

            // 페이코 로그인을 사용하는데 만약 유저 정보가 모두 기입이 안된 상황이라면 에러 메세지와 로그인 페이지로 돌려보냄
            if (isPaycoLogin(oauthToken)) {
                if (isUserInfoIncomplete(oauth2User)) {
                    handleIncompletePaycoUserInfo(request, response, oauthToken);
                    request.setAttribute("error", "페이코 로그인 시 개인정보를 모두 동의 해주셔야 하고, 페이코 계정에서도 회원의 정보가 기입이 된 상태여야 합니다.");
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/login");
                    dispatcher.forward(request, response);
                    return;
                }
            }

            // ouath2 같은 경우에는 아이디와 비빈을 모두 같게 설정함
            String oauth2IdAndPwd = buildOauth2IdAndPwd(oauth2User);
            log.debug("Oauth2 ID and Password: {}", oauth2IdAndPwd);

            AccessRefreshTokenDTO accessRefreshTokenDTO = tokenAdapter.login(new LoginRequest(oauth2IdAndPwd, oauth2IdAndPwd));

            if (Objects.isNull(accessRefreshTokenDTO) && isUserInfoIncomplete(oauth2User)) {
                accessRefreshTokenDTO = handleNewUserRegistration(oauth2User, oauth2IdAndPwd, oauthToken);
            }

            if (accessRefreshTokenDTO != null) {
                handleSuccessfulLogin(request , response, accessRefreshTokenDTO);
            }
        }

        // 여기서 send redirect 를 사용했었는데 send redirect 는 토큰 검증 필터를 통과하지 않기에 dispatcher 사용함
        RequestDispatcher dispatcher = request.getRequestDispatcher("/?clearLocalCart=true");
        dispatcher.forward(request, response);
    }

    private String getUserRole(OAuth2User oauth2User) {
        return oauth2User.getAuthorities().stream().findFirst().get().getAuthority();
    }

    private boolean isPaycoLogin(OAuth2AuthenticationToken oauthToken) {
        return oauthToken.getAuthorizedClientRegistrationId().equals("payco");
    }

    private boolean isUserInfoIncomplete(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        return attributes.values().stream().anyMatch(value -> value == null || value.toString().isBlank());
    }

    private void handleIncompletePaycoUserInfo(HttpServletRequest request, HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws ServletException, IOException {

        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId, oauthToken.getName()
        );

        String clientId = authorizedClient.getClientRegistration().getClientId();
        log.error("clientId: {}", clientId);

        // 접근 토큰을 가져와서
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        log.error("accessToken: {}", accessToken);


        paycoUserService.revokeOfferAgreement(clientId, accessToken);

        authorizedClientService.removeAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

    }

    private String buildOauth2IdAndPwd(OAuth2User oauth2User) {
        return oauth2User.getAttribute("provider") + "_" + oauth2User.getAttribute("providerId");
    }

    private AccessRefreshTokenDTO handleNewUserRegistration(OAuth2User oauth2User, String oauth2IdAndPwd, OAuth2AuthenticationToken oauthToken) {
        LocalDate birthday = extractBirthday(oauth2User);
        MemberRequestDTO memberRequestDTO = new MemberRequestDTO(
                oauth2IdAndPwd,
                passwordEncoder.encode(oauth2IdAndPwd),
                oauth2User.getAttribute("name").toString(),
                oauth2User.getAttribute("phone").toString(),
                oauth2User.getAttribute("email").toString(),
                birthday,
                null,
                "1",
                "1"
        );

        memberAdapter.createMember(memberRequestDTO);
        log.debug("New member created: {}", memberRequestDTO);

        return tokenAdapter.login(new LoginRequest(oauth2IdAndPwd, oauth2IdAndPwd));
    }

    private LocalDate extractBirthday(OAuth2User oauth2User) {
        String birthdayStr = oauth2User.getAttribute("birthday").toString();
        int month, day, year = 0;

        if (isPaycoLogin((OAuth2AuthenticationToken) oauth2User)) {
            month = Integer.parseInt(birthdayStr.substring(0, 2));
            day = Integer.parseInt(birthdayStr.substring(2, 4));
            return LocalDate.of(0, month, day);
        } else {
            year = Integer.parseInt(birthdayStr.substring(0, 4));
            month = Integer.parseInt(birthdayStr.substring(4, 6));
            day = Integer.parseInt(birthdayStr.substring(6, 8));
            return LocalDate.of(year, month, day);
        }
    }

    private void handleSuccessfulLogin(HttpServletRequest request , HttpServletResponse response, AccessRefreshTokenDTO accessRefreshTokenDTO) {
        String accessToken = accessRefreshTokenDTO.accessToken();
        String refreshToken = accessRefreshTokenDTO.refreshToken();

        CookieUtil.addResponseAccessTokenCookie(response, accessToken, jwtProperties.getAccessTokenExpirationTime());
        CookieUtil.addResponseRefreshTokenCookie(response, refreshToken, jwtProperties.getRefreshTokenExpirationTime());

        tokenAdapter.saveToken(jwtProperties.getTokenPrefix() + " " + refreshToken);

        memberAdapter.loginMember(jwtUtils.getUsernameFromAccessToken(accessToken));
    }
}

