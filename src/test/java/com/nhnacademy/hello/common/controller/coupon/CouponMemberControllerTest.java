package com.nhnacademy.hello.common.controller.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.coupon.CouponMemberController;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CouponMemberController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class // 글로벌 예외 처리기 제외
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CouponMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponMemberAdapter couponMemberAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /members/{memberId}/coupons - Success")
    void testGetMemberCoupons_Success() throws Exception {
        String memberId = "member123";
        List<CouponMemberDTO> mockCoupons = Arrays.asList(
                new CouponMemberDTO(1L, 2L),
                new CouponMemberDTO(2L, 3L)
        );

        doReturn(mockCoupons).when(couponMemberAdapter).getMemberCoupons(memberId);

        mockMvc.perform(get("/members/{memberId}/coupons", memberId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(mockCoupons.size()))
                .andExpect(jsonPath("$[0].MemberCouponId").value(1L))
                .andExpect(jsonPath("$[0].couponId").value(2L))
                .andExpect(jsonPath("$[1].MemberCouponId").value(2L))
                .andExpect(jsonPath("$[1].couponId").value(3L));
    }

    @Test
    @DisplayName("POST /members/{memberId}/coupons/{couponId} - Success")
    void testCreateMemberCoupon_Success() throws Exception {
        String memberId = "member123";
        Long couponId = 1L;

        // 어댑터의 createMemberCoupon 메서드가 정상적으로 동작하도록 모킹
        doNothing().when(couponMemberAdapter).createMemberCoupon(memberId, couponId);

        mockMvc.perform(post("/members/{memberId}/coupons/{couponId}", memberId, couponId))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Void 반환 시 빈 본문
    }

    @Test
    @DisplayName("DELETE /members/{memberId}/coupons/{couponId} - Success")
    void testDeleteMemberCoupon_Success() throws Exception {
        String memberId = "member123";
        Long couponId = 1L;

        // void 메서드 스텁 설정: 아무 동작도 하지 않도록 설정
        doNothing().when(couponMemberAdapter).deleteMemberCoupon(memberId, couponId);

        mockMvc.perform(delete("/members/{memberId}/coupons/{couponId}", memberId, couponId))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Void 반환 시 빈 본문
    }

    @Test
    @DisplayName("GET /members/{memberId}/coupons - Member Not Found")
    void testGetMemberCoupons_MemberNotFound() throws Exception {
        String memberId = "nonexistentMember";

        // 어댑터가 빈 리스트를 반환하도록 모킹
        doReturn(Arrays.asList()).when(couponMemberAdapter).getMemberCoupons(memberId);

        mockMvc.perform(get("/members/{memberId}/coupons", memberId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    // 예외 처리를 검증하는 테스트 케이스는 제외했습니다.
}
