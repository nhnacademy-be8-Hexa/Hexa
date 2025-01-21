package com.nhnacademy.hello.controller.point;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.PointPolicyAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.point.PointPolicyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PointPolicyControllerTest {

    @Mock
    private PointPolicyAdapter pointPolicyAdapter;

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private Model model;

    @InjectMocks
    private PointPolicyController pointPolicyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("포인트 정책 관리 페이지 접근 테스트")
    void testAdminPointPolicyPage() {
        // Given
        MemberDTO memberDTO = new MemberDTO("admin", "Admin User", "010-1234-5678", "admin@example.com", null, null, null, "ADMIN", null, null);
        List<PointPolicyDTO> policies = List.of(new PointPolicyDTO("Policy1", 10), new PointPolicyDTO("Policy2", -5));

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn("admin");

            when(memberAdapter.getMember("admin")).thenReturn(memberDTO);
            when(pointPolicyAdapter.getAllPointPolicies()).thenReturn(ResponseEntity.ok(policies));

            // When
            String viewName = pointPolicyController.adminPointPolicyPage(model);

            // Then
            assertEquals("admin/point", viewName);
            verify(model).addAttribute("member", memberDTO);
            verify(model).addAttribute("policies", policies);
        }
    }

    @Test
    @DisplayName("포인트 정책 생성 페이지 접근 테스트")
    void testCreatePointPolicyForm() {
        // When
        String viewName = pointPolicyController.createPointPolicyForm(model);

        // Then
        assertEquals("admin/createPointPolicy", viewName);
        verify(model).addAttribute("pointPolicyDTO", new PointPolicyDTO("", 0));
    }

    @Test
    @DisplayName("포인트 정책 생성 테스트")
    void testCreatePointPolicy() {
        // Given
        String pointPolicyName = "Policy1";
        int pointDelta = 10;

        // When
        String viewName = pointPolicyController.createPointPolicy(pointPolicyName, pointDelta, model);

        // Then
        assertEquals("redirect:/admin/point", viewName);
        verify(pointPolicyAdapter).createPointPolicy(new PointPolicyDTO(pointPolicyName, pointDelta));
    }

    @Test
    @DisplayName("포인트 정책 수정 페이지 접근 테스트")
    void testEditPointPolicyForm() {
        // Given
        String pointPolicyName = "Policy1";
        PointPolicyDTO policy = new PointPolicyDTO(pointPolicyName, 10);

        when(pointPolicyAdapter.getAllPointPolicies()).thenReturn(ResponseEntity.ok(List.of(policy)));

        // When
        String viewName = pointPolicyController.editPointPolicyForm(pointPolicyName, model);

        // Then
        assertEquals("admin/editPointPolicy", viewName);
        verify(model).addAttribute("pointPolicyDTO", policy);
    }

    @Test
    @DisplayName("포인트 정책 수정 테스트")
    void testUpdatePointPolicy() {
        // Given
        String pointPolicyName = "Policy1";
        int pointDelta = 20;

        // When
        String viewName = pointPolicyController.updatePointPolicy(pointPolicyName, pointDelta);

        // Then
        assertEquals("redirect:/admin/point", viewName);
        verify(pointPolicyAdapter).updatePointPolicy(new PointPolicyDTO(pointPolicyName, pointDelta));
    }

    @Test
    @DisplayName("포인트 정책 삭제 테스트")
    void testDeletePointPolicy() {
        // Given
        String pointPolicyName = "Policy1";

        // When
        String viewName = pointPolicyController.deletePointPolicy(pointPolicyName);

        // Then
        assertEquals("redirect:/admin/point", viewName);
        verify(pointPolicyAdapter).deletePointPolicy(pointPolicyName);
    }
}
