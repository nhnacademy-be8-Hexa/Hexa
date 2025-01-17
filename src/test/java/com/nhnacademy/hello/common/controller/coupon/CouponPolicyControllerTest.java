package com.nhnacademy.hello.common.controller.coupon;


import com.nhnacademy.hello.common.feignclient.coupon.CouponPolicyAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.coupon.CouponController;
import com.nhnacademy.hello.controller.coupon.CouponPolicyController;
import com.nhnacademy.hello.dto.coupon.CouponPolicyDTO;
import com.nhnacademy.hello.dto.coupon.CouponPolicyRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CouponPolicyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CouponPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponPolicyAdapter couponPolicyAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("GET /coupon-policies - 모든 쿠폰 정책 조회")
    void testGetPolicies() throws Exception {
        // given
        List<CouponPolicyDTO> policies = List.of(
                new CouponPolicyDTO(1L, "정책1", 10, "설명1", 100, 1000, false, "이벤트1", null),
                new CouponPolicyDTO(2L, "정책2", 20, "설명2", 200, 2000, false, "이벤트2", null)
        );
        when(couponPolicyAdapter.getPolicies(false)).thenReturn(policies);

        // when & then
        mockMvc.perform(get("/coupon-policies").param("deleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(policies.size()));

        verify(couponPolicyAdapter, times(1)).getPolicies(false);
    }

    @Test
    @DisplayName("GET /coupon-policies/{policyId} - 특정 쿠폰 정책 조회")
    void testGetPolicyById() throws Exception {
        // given
        Long policyId = 1L;
        CouponPolicyDTO policy = new CouponPolicyDTO(1L, "정책1", 10, "설명1", 100, 1000, false, "이벤트1", null);
        when(couponPolicyAdapter.getPolicyById(policyId)).thenReturn(policy);

        // when & then
        mockMvc.perform(get("/coupon-policies/{policyId}", policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponPolicyId").value(policy.getCouponPolicyId()))
                .andExpect(jsonPath("$.couponPolicyName").value(policy.getCouponPolicyName()));

        verify(couponPolicyAdapter, times(1)).getPolicyById(policyId);
    }

    @Test
    @DisplayName("GET /coupon-policies/{eventType}/eventType - 특정 이벤트 타입 쿠폰 정책 조회")
    void testGetPolicyByEventType() throws Exception {
        // given
        String eventType = "이벤트1";
        CouponPolicyDTO policy = new CouponPolicyDTO(1L, "정책1", 10, "설명1", 100, 1000, false, "이벤트1", null);
        when(couponPolicyAdapter.getPolicyByEventType(eventType)).thenReturn(policy);

        // when & then
        mockMvc.perform(get("/coupon-policies/{eventType}/eventType", eventType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value(policy.getEventType()));

        verify(couponPolicyAdapter, times(1)).getPolicyByEventType(eventType);
    }

    @Test
    @DisplayName("POST /coupon-policies - 쿠폰 정책 생성")
    void testCreatePolicy() throws Exception {
        // given
        CouponPolicyDTO createdPolicy = new CouponPolicyDTO(1L, "정책1", 10, "설명1", 100, 1000, false, "이벤트1", null);
        when(couponPolicyAdapter.createPolicy(any(CouponPolicyRequestDTO.class))).thenReturn(createdPolicy);

        // when & then
        mockMvc.perform(post("/coupon-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"couponPolicyName\":\"정책1\"," +
                                "\"minPurchaseAmount\":1000," +
                                "\"discountType\":\"퍼센트\"," +
                                "\"discountValue\":10," +
                                "\"maxDiscountAmount\":5000," +
                                "\"eventType\":\"이벤트1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponPolicyId").value(createdPolicy.getCouponPolicyId()));

        verify(couponPolicyAdapter, times(1)).createPolicy(any(CouponPolicyRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /coupon-policies/{policyId} - 쿠폰 정책 수정")
    void testUpdatePolicy() throws Exception {
        // given
        Long policyId = 1L;
        CouponPolicyRequestDTO requestDTO = new CouponPolicyRequestDTO("정책 수정", 15, "수정 설명", 150, 1500, "이벤트 수정");
        CouponPolicyDTO updatedPolicy = new CouponPolicyDTO(1L, "정책 수정", 15, "수정 설명", 150, 1500, false, "이벤트 수정", null);
        when(couponPolicyAdapter.updatePolicy(eq(policyId), any(CouponPolicyRequestDTO.class))).thenReturn(updatedPolicy);

        // when & then
        mockMvc.perform(put("/coupon-policies/{policyId}", policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"couponPolicyName\":\"정책1\"," +
                                "\"minPurchaseAmount\":1000," +
                                "\"discountType\":\"퍼센트\"," +
                                "\"discountValue\":10," +
                                "\"maxDiscountAmount\":5000," +
                                "\"eventType\":\"이벤트1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponPolicyName").value(updatedPolicy.getCouponPolicyName()));

        verify(couponPolicyAdapter, times(1)).updatePolicy(eq(policyId), any(CouponPolicyRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /coupon-policies/{policyId} - 쿠폰 정책 삭제")
    void testDeletePolicy() throws Exception {
        // given
        Long policyId = 1L;
        Map<String, String> response = Map.of("message", "쿠폰 정책이 삭제되었습니다.");
        when(couponPolicyAdapter.deletePolicy(policyId)).thenReturn(response);

        // when & then
        mockMvc.perform(delete("/coupon-policies/{policyId}", policyId))
                .andExpect(status().isOk()); // 200 ok 확인

        verify(couponPolicyAdapter, times(1)).deletePolicy(policyId); // 호출 검증
    }

}
