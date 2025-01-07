package com.nhnacademy.hello.controller.login;

import com.nhnacademy.hello.common.feignclient.auth.TokenAdapter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.CookieUtil;
import com.nhnacademy.hello.dto.jwt.AccessRefreshTokenDTO;
import com.nhnacademy.hello.dto.member.LoginRequest;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import com.nhnacademy.hello.service.MessageSendService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final JwtProperties jwtProperties;
    private final MemberAdapter memberAdapter;
    private final TokenAdapter tokenAdapter;
    private final MessageSendService messageSendService;
    private final CookieUtil cookieUtil;

    private static final String AUTH_CODE_SESSION_KEY = "authCode";
    private static final String AUTH_CODE_TIMESTAMP_KEY = "timestamp";

    @GetMapping("/login")
    public String login() {
        return "login";
    }


    @PostMapping("/login/process")
    public String process(
            @ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response,
            Model model,
            HttpSession session
    ) throws IOException {
        // 인증 서버에 로그인 요청을 하고 토큰을 받는다.
        AccessRefreshTokenDTO accessRefreshTokenDTO = tokenAdapter.login(loginRequest);

        // 토큰 예외처리 (로그인 실패)
        if(accessRefreshTokenDTO == null || accessRefreshTokenDTO.accessToken()==null) {
            log.error("Authorization failure id: {}", loginRequest.id());
            model.addAttribute("error", "로그인 실패!");
            return "login";
        }

        // 마지막 로그인으로부터 3개월 경과했는지 검증
        MemberDTO memberDTO = memberAdapter.getMember(loginRequest.id());
        LocalDateTime memberLastLoginAt = memberDTO.memberLastLoginAt();

        if(Objects.nonNull(memberLastLoginAt) && memberLastLoginAt.isBefore(LocalDateTime.now().minusMonths(3))
                && memberDTO.memberStatus().statusId() != 2) {
            // 'DORMANT' 상태로 전환
            try {
                memberAdapter.updateMember(loginRequest.id(),
                        new MemberUpdateDTO(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "2" // 'DORMANT' 상태 ID
                        )
                );
                memberDTO = memberAdapter.getMember(loginRequest.id());
            } catch (Exception e) {
                log.error("Failed to set member to dormant for id: {}", loginRequest.id(), e);
                model.addAttribute("error", "휴면 계정 전환에 실패했습니다!");
                return "login";
            }
        }

        if(memberDTO.memberStatus().statusId() == 2) {
            // 4자리 랜덤 숫자 생성
            int authCode = new Random().nextInt(9000) + 1000; // 1000 ~ 9999
            String message = String.format("다음 인증번호를 입력하시오: %04d", authCode);

            // 메시지 전송
            try {
                messageSendService.sendMessage(message);
            } catch (Exception e) {
                log.error("Failed to send auth message to member id: {}", loginRequest.id(), e);
                model.addAttribute("error", "인증 메시지 전송에 실패했습니다!");
                return "login";
            }

            // 인증 코드를 세션에 저장
            session.setAttribute(AUTH_CODE_SESSION_KEY, authCode);
            session.setAttribute(AUTH_CODE_TIMESTAMP_KEY, LocalDateTime.now());
            session.setAttribute("memberId", loginRequest.id());

            // dormancy.html로 이동
            return "redirect:/member/dormancy";
        }

        // 로그인 시간 업데이트 요청을 보낸다
        try {
            memberAdapter.loginMember(loginRequest.id());
        } catch (Exception e) {
            log.error("Failed to update login time for member id: {}", loginRequest.id(), e);
            model.addAttribute("error", "로그인 실패!");
            return "login";
        }


        String access_token = accessRefreshTokenDTO.accessToken();
        String refresh_token = accessRefreshTokenDTO.refreshToken();


        // 토큰을 쿠키에 저장한다
        cookieUtil.addResponseAccessTokenCookie(response,access_token,jwtProperties.getAccessTokenExpirationTime());
        cookieUtil.addResponseRefreshTokenCookie(response,refresh_token,jwtProperties.getRefreshTokenExpirationTime());

        tokenAdapter.saveToken(jwtProperties.getTokenPrefix() + " " +refresh_token);



        // 로그인 후 홈페이지로 이동

        return "redirect:/?clearLocalCart=true";
    }

    @GetMapping("/member/dormancy")
    public String dormancyPage() {
        return "member/dormancy";
    }

    @PostMapping("/login/verify")
    public String verifyAuthCode(
            @RequestParam("authCode") String authCodeInput,
            HttpSession session,
            Model model
    ) throws IOException {
        Integer authCodeStored = (Integer) session.getAttribute(AUTH_CODE_SESSION_KEY);
        LocalDateTime authCodeTimestamp = (LocalDateTime) session.getAttribute(AUTH_CODE_TIMESTAMP_KEY);
        String memberId = (String) session.getAttribute("memberId");

        if(authCodeStored == null || memberId == null || authCodeTimestamp == null) {
            model.addAttribute("error", "인증 코드가 만료되었습니다. 다시 로그인해주세요.");
            return "/member/dormancy";
        }

        // 현재 시간과 인증 코드 생성 시간 비교 (5분 이내)
        LocalDateTime now = LocalDateTime.now();
        if(authCodeTimestamp.plusMinutes(5).isBefore(now)) {
            // 인증 코드 만료
            session.removeAttribute(AUTH_CODE_SESSION_KEY);
            session.removeAttribute(AUTH_CODE_TIMESTAMP_KEY);
            session.removeAttribute("memberId");
            model.addAttribute("error", "인증 코드가 만료되었습니다. 다시 로그인해주세요.");
            return "member/dormancy";
        }

        if(authCodeStored.toString().equals(authCodeInput)) {
            // 인증 성공 후, 세션에서 인증 코드와 회원 ID 제거
            session.removeAttribute(AUTH_CODE_SESSION_KEY);
            session.removeAttribute(AUTH_CODE_TIMESTAMP_KEY);
            session.removeAttribute("memberId");

            // 로그인 시간 업데이트 요청을 보낸다
            try {
                memberAdapter.loginMember(memberId);
                memberAdapter.updateMember(memberId, new MemberUpdateDTO(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "1"
                ));
            } catch (Exception e) {
                log.error("Failed to update login time for member id: {}", memberId, e);
                model.addAttribute("error", "로그인 실패!");
                return "login";
            }


            // 로그인 후 홈페이지로 이동
            return "redirect:/?clearLocalCart=true";
        } else {
            model.addAttribute("error", "인증 번호가 일치하지 않습니다.");
            return "member/dormancy";
        }
    }
}
