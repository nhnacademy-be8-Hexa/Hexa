package com.nhnacademy.hello.common.controller.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.coupon.CouponConsumer;
import com.nhnacademy.hello.controller.coupon.CouponMemberController;
import com.nhnacademy.hello.controller.coupon.EventController;
import com.nhnacademy.hello.dto.member.MemberDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = EventController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class // 글로벌 예외 처리기 제외
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponMemberAdapter couponMemberAdapter;

    @MockBean
    private MemberAdapter memberAdapter;

    @MockBean
    private CouponAdapter couponAdapter;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private CouponConsumer couponGet;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * GET /event - 사용자가 로그인하지 않은 경우
     * 예상 결과: /login으로 리다이렉트
     */
    @Test
    @DisplayName("GET /event - Not Logged In")
    void testGetEvent_NotLoggedIn() throws Exception {
        try (MockedStatic<AuthInfoUtils> mockedAuth = Mockito.mockStatic(AuthInfoUtils.class)) {
            // 사용자가 로그인하지 않았다고 모킹
            mockedAuth.when(AuthInfoUtils::isLogin).thenReturn(false);

            mockMvc.perform(get("/event"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    /**
     * GET /event - 사용자가 ADMIN으로 로그인한 경우
     * 예상 결과: "event/event" 뷰 반환, model에 isAdmin=true
     */
    @Test
    @DisplayName("GET /event - Logged In as ADMIN")
    void testGetEvent_LoggedInAsAdmin() throws Exception {
        String username = "adminUser";
        String memberId = "admin123";
        String memberRole = "ADMIN";

        try (MockedStatic<AuthInfoUtils> mockedAuth = Mockito.mockStatic(AuthInfoUtils.class)) {
            // 사용자가 로그인했고, username을 반환하도록 모킹
            mockedAuth.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuth.when(AuthInfoUtils::getUsername).thenReturn(username);

            // MemberAdapter가 특정 사용자에 대한 정보를 반환하도록 모킹
            MemberDTO memberDTO = new MemberDTO(
                    memberId,
                    "Test Name",                 // memberName
                    "1234567890",                // memberNumber
                    "test@example.com",          // memberEmail
                    LocalDate.of(1990, 1, 1),    // memberBirthAt
                    LocalDate.now(),             // memberCreatedAt
                    LocalDateTime.now(),         // memberLastLoginAt
                    memberRole,
                    null,                        // rating (필요 시 더미 인스턴스로 대체)
                    null                         // memberStatus (필요 시 더미 인스턴스로 대체)
            );
            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            mockMvc.perform(get("/event"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("event/event"))
                    .andExpect(model().attribute("isAdmin", true));

            // 메서드 호출 검증
            verify(memberAdapter, times(1)).getMember(username);
        }
    }

    /**
     * GET /event - 사용자가 ADMIN이 아닌 일반 사용자로 로그인한 경우
     * 예상 결과: "event/event" 뷰 반환, model에 isAdmin=false
     */
    @Test
    @DisplayName("GET /event - Logged In as Non-Admin")
    void testGetEvent_LoggedInAsNonAdmin() throws Exception {
        String username = "regularUser";
        String memberId = "user123";
        String memberRole = "USER";

        try (MockedStatic<AuthInfoUtils> mockedAuth = Mockito.mockStatic(AuthInfoUtils.class)) {
            // 사용자가 로그인했고, username을 반환하도록 모킹
            mockedAuth.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuth.when(AuthInfoUtils::getUsername).thenReturn(username);

            // MemberAdapter가 특정 사용자에 대한 정보를 반환하도록 모킹
            MemberDTO memberDTO = new MemberDTO(
                    memberId,
                    "Test Name",                 // memberName
                    "1234567890",                // memberNumber
                    "test@example.com",          // memberEmail
                    LocalDate.of(1990, 1, 1),    // memberBirthAt
                    LocalDate.now(),             // memberCreatedAt
                    LocalDateTime.now(),         // memberLastLoginAt
                    memberRole,
                    null,                        // rating (필요 시 더미 인스턴스로 대체)
                    null                         // memberStatus (필요 시 더미 인스턴스로 대체)
            );
            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            mockMvc.perform(get("/event"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("event/event"))
                    .andExpect(model().attribute("isAdmin", false));

            // 메서드 호출 검증
            verify(memberAdapter, times(1)).getMember(username);
        }
    }

    /**
     * POST /coupon-get - 사용자가 로그인하지 않은 경우
     * 예상 결과: /login으로 리다이렉트
     */
    @Test
    @DisplayName("POST /coupon-get - Not Logged In")
    void testIssueCoupon_NotLoggedIn() throws Exception {
        try (MockedStatic<AuthInfoUtils> mockedAuth = Mockito.mockStatic(AuthInfoUtils.class)) {
            // 사용자가 로그인하지 않았다고 모킹
            mockedAuth.when(AuthInfoUtils::isLogin).thenReturn(false);

            mockMvc.perform(post("/coupon-get"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    /**
     * POST /coupon-get - 사용자가 로그인한 경우 쿠폰 발급 성공
     * 예상 결과: /event로 리다이렉트, 플래시 속성에 couponMessage 추가
     */
    @Test
    @DisplayName("POST /coupon-get - Success")
    void testIssueCoupon_Success() throws Exception {
        String username = "regularUser";
        String memberId = "user123";
        String couponMsg = "Coupon Issued Successfully";
        String memberRole = "USER";

        try (MockedStatic<AuthInfoUtils> mockedAuth = Mockito.mockStatic(AuthInfoUtils.class)) {
            // 사용자가 로그인했고, username을 반환하도록 모킹
            mockedAuth.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuth.when(AuthInfoUtils::getUsername).thenReturn(username);

            // MemberAdapter가 특정 사용자에 대한 정보를 반환하도록 모킹
            MemberDTO memberDTO = new MemberDTO(
                    memberId,
                    "Test Name",                 // memberName
                    "1234567890",                // memberNumber
                    "test@example.com",          // memberEmail
                    LocalDate.of(1990, 1, 1),    // memberBirthAt
                    LocalDate.now(),             // memberCreatedAt
                    LocalDateTime.now(),         // memberLastLoginAt
                    memberRole,
                    null,                        // rating (필요 시 더미 인스턴스로 대체)
                    null                         // memberStatus (필요 시 더미 인스턴스로 대체)
            );
            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            // RabbitTemplate의 convertAndSend 메서드를 아무 동작도 하지 않도록 스텁
            doNothing().when(rabbitTemplate).convertAndSend("hexa.coupon.exchanges", "hexa.coupon.binding", memberId);

            // CouponConsumer의 couponGet 메서드가 특정 메시지를 반환하도록 스텁
            when(couponGet.couponGet()).thenReturn(couponMsg);

            mockMvc.perform(post("/coupon-get"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/event"))
                    .andExpect(flash().attribute("couponMessage", couponMsg));

            // 메서드 호출 검증
            verify(memberAdapter, times(1)).getMember(username);
            verify(rabbitTemplate, times(1)).convertAndSend("hexa.coupon.exchanges", "hexa.coupon.binding", memberId);
            verify(couponGet, times(1)).couponGet();
        }
    }
}
