package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CouponPageControllerTest {

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private Model model;

    private CouponPageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CouponPageController(memberAdapter);
    }

    private MemberDTO createMemberDTO(String memberId, String memberRole) {
        return new MemberDTO(
                memberId,
                "Test Name",
                "123-4567-890",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                LocalDateTime.now(),
                memberRole,
                new RatingDTO(1L, "Gold", 90),
                new MemberStatusDTO(1L, "Active")
        );
    }

    @AfterEach
    void tearDown() {
        clearAllCaches(); // 모든 캐시 초기화
    }

    @Test
    @DisplayName("관리자 쿠폰 페이지 - 성공")
    void adminPage_Success() {
        // Given
        String username = "adminUser";
        MemberDTO memberDTO = createMemberDTO(username, "ADMIN");

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            // Mock AuthInfoUtils
            mockedStatic.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn(username);

            // Mock MemberAdapter
            when(memberAdapter.getMember(username)).thenReturn(memberDTO);

            // When
            String viewName = controller.adminPage(model);

            // Then
            assertEquals("admin/coupon", viewName);
            verify(model).addAttribute("member", memberDTO);
        }
    }

    @Test
    @DisplayName("관리자 쿠폰 페이지 - 로그인되지 않은 경우 리다이렉트")
    void adminPage_NotLoggedIn() {
        // Given
        mockStatic(AuthInfoUtils.class);
        when(AuthInfoUtils.isLogin()).thenReturn(false);

        // When
        String viewName = controller.adminPage(model);

        // Then
        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(model);
    }

    @Test
    @DisplayName("관리자 쿠폰 페이지 - 관리자가 아닌 경우 리다이렉트")
    void adminPage_NotAdmin() {
        // Given
        String username = "regularUser";
        MemberDTO memberDTO = createMemberDTO(username, "USER");

        // Mock AuthInfoUtils
        mockStatic(AuthInfoUtils.class);
        when(AuthInfoUtils.isLogin()).thenReturn(true);
        when(AuthInfoUtils.getUsername()).thenReturn(username);

        // Mock MemberAdapter
        when(memberAdapter.getMember(username)).thenReturn(memberDTO);

        // When
        String viewName = controller.adminPage(model);

        // Then
        assertEquals("redirect:/index", viewName);
        verifyNoInteractions(model);
    }
}