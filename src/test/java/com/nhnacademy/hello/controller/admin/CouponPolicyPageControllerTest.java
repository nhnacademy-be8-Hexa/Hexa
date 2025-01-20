package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponPolicyPageControllerTest {

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private Model model;

    private CouponPolicyPageController controller;

    @BeforeEach
    void setUp() {
        memberAdapter = mock(MemberAdapter.class);
        model = mock(Model.class);
        controller = new CouponPolicyPageController(memberAdapter);
    }

    @Test
    @DisplayName("관리자 쿠폰 정책 페이지 - 성공")
    void adminPage_Success() {
        // Given
        String username = "adminUser";
        MemberDTO memberDTO = createMemberDTO(username, "ADMIN");

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn(username);

            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            // When
            String viewName = controller.adminPage(model);

            // Then
            assertEquals("admin/coupon-policy", viewName);
            verify(model).addAttribute("member", memberDTO);
        }
    }

    @Test
    @DisplayName("관리자 쿠폰 정책 페이지 - 로그인되지 않은 경우")
    void adminPage_NotLoggedIn() {
        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            // Mock AuthInfoUtils
            mockedStatic.when(AuthInfoUtils::isLogin).thenReturn(false);

            // When
            String viewName = controller.adminPage(model);

            // Then
            assertEquals("redirect:/login", viewName);
            verifyNoInteractions(model);
        }
    }

    @Test
    @DisplayName("관리자 쿠폰 정책 페이지 - 관리자가 아닌 경우")
    void adminPage_NotAdmin() {
        // Given
        String username = "user1";
        MemberDTO memberDTO = createMemberDTO(username, "USER");

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn(username);

            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            // When
            String viewName = controller.adminPage(model);

            // Then
            assertEquals("redirect:/index", viewName);
            verifyNoInteractions(model);
        }
    }

    private MemberDTO createMemberDTO(String username, String role) {
        return new MemberDTO(
                username,
                "Test Name",
                "010-1234-5678",
                "test@example.com",
                null,
                null,
                null,
                role,
                new RatingDTO(1L, "Gold", 10),
                new MemberStatusDTO(1L, "Active")
        );
    }
}