package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.DeliveryCostPolicyAdapter;
import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyDTO;
import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryCostPolicyController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class DeliveryCostPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryCostPolicyAdapter deliveryCostPolicyAdapter;

    // 테스트에서 사용될 DTO들
    private DeliveryCostPolicyDTO policyDTO;

    @BeforeEach
    void setUp() {
        // 공통적으로 사용할 객체 초기화
        policyDTO = new DeliveryCostPolicyDTO(1L,
                5000,
                30000,
                "admin",
                LocalDateTime.now()
                );

        // Mock 설정
        when(deliveryCostPolicyAdapter.getCount()).thenReturn(ResponseEntity.ok(10L));
        when(deliveryCostPolicyAdapter.getAll(anyInt(), anyInt(), anyString())).thenReturn(ResponseEntity.ok(List.of(policyDTO)));
    }

    @Test
    @DisplayName("배송비 정책 페이지")
    void testDeliveryCostPolicyPage() throws Exception {
        mockMvc.perform(get("/admin/delivery-cost-policy")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/deliveryCostPolicyManage"))
                .andExpect(model().attributeExists("policyList"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2));

        // Mocked 메서드가 호출되었는지 확인
        verify(deliveryCostPolicyAdapter, times(1)).getCount();
        verify(deliveryCostPolicyAdapter, times(1)).getAll(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("배송비 정책 생성")
    void testDeliveryCostPolicySave() throws Exception {
        // requestDto를 JSON으로 변환하여 전송
        mockMvc.perform(post("/admin/delivery-cost-policy")
                        .contentType("application/json")
                        .content("{\"deliveryCost\":5000, \"freeMinimumAmount\":30000}"))
                .andExpect(status().isOk());

        // create 메서드가 호출되었는지 확인
        verify(deliveryCostPolicyAdapter, times(1)).create(any());
    }


}