package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberManageControllerTest {

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    private MemberManageController controller;

    @BeforeEach
    void setUp() {
        memberAdapter = mock(MemberAdapter.class);
        model = mock(Model.class);
        bindingResult = mock(BindingResult.class);
        controller = new MemberManageController(memberAdapter);
    }

    @Test
    @DisplayName("멤버 목록 조회 - 성공")
    void getMembers_Success() {
        // Given
        List<MemberDTO> members = List.of(createMemberDTO("user1"), createMemberDTO("user2"));
        when(memberAdapter.getMembers(0, 10, null)).thenReturn(members);
        when(memberAdapter.getMemberCount(null)).thenReturn(ResponseEntity.ok(2L));

        // When
        String viewName = controller.getMembers(1, 10, null, model);

        // Then
        assertEquals("admin/memberManage", viewName);
        verify(model).addAttribute("members", members);
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 1);
    }

    @Test
    @DisplayName("특정 멤버 상세 조회 - 성공")
    void getMemberDetail_Success() {
        // Given
        MemberDTO member = createMemberDTO("user1");
        when(memberAdapter.getMember("user1")).thenReturn(member);

        // When
        String viewName = controller.getMemberDetail("user1", model);

        // Then
        assertEquals("admin/memberDetail", viewName);
        verify(model).addAttribute("member", member);
    }

    @Test
    @DisplayName("멤버 수정 폼 - 성공")
    void getUpdateForm_Success() {
        // Given
        MemberDTO member = createMemberDTO("user1");
        List<RatingDTO> ratings = List.of(new RatingDTO(1L, "Gold", 10));
        List<MemberStatusDTO> memberStatuses = List.of(new MemberStatusDTO(1L, "Active"));

        when(memberAdapter.getMember("user1")).thenReturn(member);
        when(memberAdapter.getRatings()).thenReturn(ratings);
        when(memberAdapter.getMemberStatus()).thenReturn(memberStatuses);

        // When
        String viewName = controller.getUpdateForm("user1", model);

        // Then
        assertEquals("admin/memberUpdateForm", viewName);
        verify(model).addAttribute(eq("updateDTO"), any(MemberUpdateDTO.class));
        verify(model).addAttribute(eq("ratings"), eq(ratings));
        verify(model).addAttribute(eq("memberStatuses"), eq(memberStatuses));
    }

    @Test
    @DisplayName("멤버 수정 요청 - 성공")
    void updateMember_Success() {
        // Given
        MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                null, "Updated Name", "010-1234-5678", "updated@example.com",
                LocalDate.of(1990, 1, 1), "1", "1"
        );

        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String viewName = controller.updateMember("user1", updateDTO, bindingResult, model);

        // Then
        assertEquals("redirect:/admin/members", viewName);
        verify(memberAdapter).updateMember("user1", updateDTO);
    }

    @Test
    @DisplayName("멤버 수정 요청 - 실패 (유효성 검사 실패)")
    void updateMember_ValidationFailure() {
        // Given
        MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                null, "Updated Name", "010-1234-5678", "updated@example.com",
                LocalDate.of(1990, 1, 1), "1", "1"
        );

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("field", "Validation error")));

        // When
        String viewName = controller.updateMember("user1", updateDTO, bindingResult, model);

        // Then
        assertEquals("redirect:/admin/members", viewName);
        verify(model).addAttribute("errors", List.of("Validation error"));
    }

    @Test
    @DisplayName("특정 멤버 탈퇴 처리 - 성공")
    void deactivateMember_Success() {
        // When
        ResponseEntity<Void> response = controller.deactivateMember("user1");

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(memberAdapter).updateMember(eq("user1"), any(MemberUpdateDTO.class));
    }

    private MemberDTO createMemberDTO(String memberId) {
        return new MemberDTO(
                memberId,
                "Test Name",
                "010-1234-5678",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                LocalDateTime.now(),
                "USER",
                new RatingDTO(1L, "Gold", 10),
                new MemberStatusDTO(1L, "Active")
        );
    }
}