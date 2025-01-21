package com.nhnacademy.hello.controller.point;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PointDetailsControllerTest {

    @Mock
    private PointDetailsAdapter pointDetailsAdapter;

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private Model model;

    @InjectMocks
    private PointDetailsController pointDetailsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("포인트 내역 페이지 로드 테스트")
    void testPointsPageLoad() {
        // Given
        String username = "testUser";
        int page = 0;
        int size = 10;
        String sort = "pointDetailsDatetime,desc";

        MemberDTO member = new MemberDTO(
                "testUser",
                "Test User",
                "010-1234-5678",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                LocalDateTime.now(),
                "USER",
                null,
                null
        );

        Long sum = 500L;
        List<PointDetailsDTO> pointDetails = List.of(
                new PointDetailsDTO(1L, new PointDetailsDTO.MemberDTO("testUser"), 200, "Earned points", LocalDateTime.now()),
                new PointDetailsDTO(2L, new PointDetailsDTO.MemberDTO("testUser"), -100, "Used points", LocalDateTime.now())
        );
        int total = 20;

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn(username);

            when(memberAdapter.getMember(username)).thenReturn(member);
            when(pointDetailsAdapter.sumPoint(username)).thenReturn(ResponseEntity.ok(sum));
            when(pointDetailsAdapter.getPointDetails(username, page, size, sort)).thenReturn(ResponseEntity.ok(pointDetails));
            when(pointDetailsAdapter.countByMemberId(username)).thenReturn(ResponseEntity.ok(total));

            // When
            String viewName = pointDetailsController.pointsPage(model, page, size, sort);

            // Then
            assertEquals("member/points", viewName);
            verify(model).addAttribute("member", member);
            verify(model).addAttribute("sum", sum);
            verify(model).addAttribute("pointDetails", pointDetails);
            verify(model).addAttribute("totalPages", 2);
            verify(model).addAttribute("total", total);
            verify(model).addAttribute("page", page);
            verify(model).addAttribute("size", size);
            verify(model).addAttribute("sort", sort);
        }
    }

    @Test
    @DisplayName("포인트 내역 페이지 로드 테스트 - 예외 처리")
    void testPointsPageLoadWithException() {
        // Given
        String username = "testUser";
        int page = 0;
        int size = 10;
        String sort = "pointDetailsDatetime,desc";

        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn(username);

            when(memberAdapter.getMember(username)).thenThrow(new RuntimeException("Member not found"));

            // When
            RuntimeException exception = null;
            try {
                pointDetailsController.pointsPage(model, page, size, sort);
            } catch (RuntimeException e) {
                exception = e;
            }

            // Then
            assertEquals("Member not found", exception.getMessage());
            verify(memberAdapter).getMember(username);
            verifyNoInteractions(pointDetailsAdapter);
        }
    }
}
