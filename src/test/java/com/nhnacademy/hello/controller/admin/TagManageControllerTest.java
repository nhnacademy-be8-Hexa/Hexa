package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.dto.tag.TagRequestDTO;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TagManageControllerTest {

    @InjectMocks
    private TagManageController tagManageController;

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private TagAdapter tagAdapter;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("태그 관리 페이지 - 관리자 권한")
    void tagList_AdminAccess() {
        // Given
        String username = "adminUser";
        MemberDTO mockMember = new MemberDTO(
                username,
                "Admin Name",
                "12345",
                "admin@example.com",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                LocalDateTime.now(),
                "ADMIN",
                null,
                null
        );
        List<TagDTO> mockTags = List.of(new TagDTO(1L, "Tag1"), new TagDTO(2L, "Tag2"));
        long totalTags = 20;

        // Static mocking을 try-with-resources로 관리
        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuthInfoUtils.when(AuthInfoUtils::getUsername).thenReturn(username);

            // Mock dependencies
            when(memberAdapter.getMember(eq(username))).thenReturn(mockMember);
            when(tagAdapter.getAllTags(eq(0), eq(10), eq(""))).thenReturn(ResponseEntity.ok(mockTags));
            when(tagAdapter.getTotalTags()).thenReturn(ResponseEntity.ok(totalTags));

            // When
            String viewName = tagManageController.tagList(1, 10, model);

            // Then
            assertEquals("admin/tagManage", viewName);
            verify(model).addAttribute("tags", mockTags);
            verify(model).addAttribute("currentPage", 1);
            verify(model).addAttribute("totalPages", 2); // 20 / 10 = 2 pages
        }
    }

    @Test
    @DisplayName("태그 관리 페이지 - 비관리자 접근 차단")
    void tagList_NonAdminAccess() {
        // Given
        String username = "normalUser";
        MemberDTO mockMember = new MemberDTO(
                username,
                "User Name",
                "54321",
                "user@example.com",
                LocalDate.of(1995, 5, 15),
                LocalDate.of(2021, 6, 1),
                LocalDateTime.now(),
                "USER",
                null,
                null
        );

        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuthInfoUtils.when(AuthInfoUtils::getUsername).thenReturn(username);

            when(memberAdapter.getMember(eq(username))).thenReturn(mockMember);

            // When
            String viewName = tagManageController.tagList(1, 10, model);

            // Then
            assertEquals("redirect:/index", viewName);
        }
    }

    @Test
    @DisplayName("태그 추가 폼 보여주기")
    void showAddTagForm() {
        // When
        String viewName = tagManageController.showAddTagForm(model);

        // Then
        assertEquals("admin/tagCreateForm", viewName);
        verify(model).addAttribute(eq("tagRequestDTO"), any(TagRequestDTO.class));
    }

    @Test
    @DisplayName("태그 추가 성공")
    void addTag_Success() {
        // Given
        TagRequestDTO tagRequestDTO = new TagRequestDTO("NewTag");
        when(tagAdapter.createTag(eq(tagRequestDTO))).thenReturn(ResponseEntity.ok(new TagDTO(1L, "NewTag")));

        // When
        String viewName = tagManageController.addTag(tagRequestDTO, model);

        // Then
        assertEquals("redirect:/admin/tagManage", viewName);
    }

    @Test
    @DisplayName("태그 추가 실패")
    void addTag_Failure() {
        // Given
        TagRequestDTO tagRequestDTO = new TagRequestDTO("NewTag");
        when(tagAdapter.createTag(eq(tagRequestDTO))).thenReturn(ResponseEntity.badRequest().build());

        // When
        String viewName = tagManageController.addTag(tagRequestDTO, model);

        // Then
        assertEquals("admin/tagCreateForm", viewName);
    }

    @Test
    @DisplayName("태그 수정 폼 보여주기")
    void showEditTagForm() {
        // Given
        Long tagId = 1L;
        List<TagDTO> mockTags = List.of(new TagDTO(tagId, "Tag1"));
        when(tagAdapter.getAllTags(eq(1), eq(10), eq(""))).thenReturn(ResponseEntity.ok(mockTags));

        // When
        String viewName = tagManageController.showEditTagForm(tagId, 1, 10, model);

        // Then
        assertEquals("admin/tagEditForm", viewName);
        verify(model).addAttribute(eq("tagRequestDTO"), any(TagRequestDTO.class));
    }

    @Test
    @DisplayName("태그 수정 성공")
    void updateTag_Success() {
        // Given
        Long tagId = 1L;
        TagRequestDTO tagRequestDTO = new TagRequestDTO("UpdatedTag");
        // updateTag() 메서드 호출에 대해 아무 동작도 하지 않도록 설정
        when(tagAdapter.updateTag(eq(tagRequestDTO), eq(tagId))).thenReturn(ResponseEntity.ok().build());

        // When
        String viewName = tagManageController.updateTag(tagId, tagRequestDTO, model);

        // Then
        assertEquals("redirect:/admin/tagManage", viewName);
        verify(tagAdapter).updateTag(eq(tagRequestDTO), eq(tagId));
    }

    @Test
    @DisplayName("태그 삭제 성공")
    void deleteTag_Success() {
        // Given
        Long tagId = 1L;
        when(tagAdapter.deleteTag(eq(tagId))).thenReturn(ResponseEntity.ok().build()); // 리턴 값을 설정

        // When
        String viewName = tagManageController.deleteTag(tagId, model);

        // Then
        assertEquals("redirect:/admin/tagManage", viewName);
        verify(tagAdapter).deleteTag(eq(tagId)); // deleteTag 호출 검증
    }
}