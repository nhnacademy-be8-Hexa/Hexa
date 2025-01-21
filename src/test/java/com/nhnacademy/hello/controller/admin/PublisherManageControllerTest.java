package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.PublisherAdapter;
import com.nhnacademy.hello.dto.book.PublisherRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PublisherManageControllerTest {

    @Mock
    private PublisherAdapter publisherAdapter;

    @InjectMocks
    private PublisherManageController publisherManageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("출판사 관리 페이지 로드 테스트")
    void testPublisherPageLoad() {
        // When
        String viewName = publisherManageController.publisher();

        // Then
        assertEquals("admin/publisherManage", viewName);
    }

    @Test
    @DisplayName("출판사 추가 테스트")
    void testAddPublisher() {
        // Given
        PublisherRequestDTO requestDTO = new PublisherRequestDTO(1L, "Test Publisher");

        // When
        String viewName = publisherManageController.addPublisher(requestDTO);

        // Then
        assertEquals("redirect:/admin/publisherManage/add", viewName);
        verify(publisherAdapter, times(1)).createPublisher(requestDTO);
    }

    @Test
    @DisplayName("출판사 추가 실패 테스트")
    void testAddPublisherFailure() {
        // Given
        PublisherRequestDTO requestDTO = new PublisherRequestDTO(1L, "Test Publisher");
        doThrow(new RuntimeException("Failed to create publisher")).when(publisherAdapter).createPublisher(requestDTO);

        // When
        RuntimeException exception = null;
        try {
            publisherManageController.addPublisher(requestDTO);
        } catch (RuntimeException e) {
            exception = e;
        }

        // Then
        assertEquals("Failed to create publisher", exception.getMessage());
        verify(publisherAdapter, times(1)).createPublisher(requestDTO);
    }
}
