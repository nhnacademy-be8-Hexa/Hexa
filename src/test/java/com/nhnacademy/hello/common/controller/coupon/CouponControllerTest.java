package com.nhnacademy.hello.common.controller.coupon;


import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.controller.coupon.CouponController;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponRequestDTO;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CouponController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponAdapter couponAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("GET /coupons/{couponId} - 쿠폰 조회")
    void testGetCouponById() throws Exception {
        // given
        Long couponId = 1L;
        CouponDTO coupon = new CouponDTO(
                1L,
                null,
                "겨울 쿠폰",
                "회원전용",
                123L,
                ZonedDateTime.now().plusDays(30),
                ZonedDateTime.now(),
                true,
                null
        );
        when(couponAdapter.getCouponById(couponId)).thenReturn(coupon);

        // when & then
        mockMvc.perform(get("/coupons/{couponId}", couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId").value(coupon.getCouponId()))
                .andExpect(jsonPath("$.couponName").value(coupon.getCouponName()));

        verify(couponAdapter, times(1)).getCouponById(couponId);
    }

    @Test
    @DisplayName("GET /coupons - 모든 쿠폰 조회")
    void testGetCouponsByActive() throws Exception {
        // given
        List<CouponDTO> coupons = List.of(
                new CouponDTO(1L, null, "쿠폰1", "회원전용", 123L, ZonedDateTime.now().plusDays(10), ZonedDateTime.now(), true, null),
                new CouponDTO(2L, null, "쿠폰2", "회원전용", 124L, ZonedDateTime.now().plusDays(15), ZonedDateTime.now(), true, null)
        );
        when(couponAdapter.getCouponsByActive(null, true)).thenReturn(coupons);

        // when & then
        mockMvc.perform(get("/coupons").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(coupons.size()));

        verify(couponAdapter, times(1)).getCouponsByActive(null, true);
    }

    @Test
    @DisplayName("POST /coupons - 쿠폰 생성")
    void testCreateCoupons() throws Exception {
        // given
        List<CouponDTO> createdCoupons = List.of(
                new CouponDTO(1L, null, "겨울 쿠폰", "회원전용", 123L, ZonedDateTime.now().plusDays(30), ZonedDateTime.now(), true, null)
        );

        when(couponAdapter.createCoupons(eq(1), any(CouponRequestDTO.class))).thenReturn(createdCoupons);

        // when & then
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("count", "1")
                        .content("{\"policyId\":1,\"couponName\":\"겨울 쿠폰\",\"couponTarget\":\"회원전용\",\"couponTargetId\":125,\"couponDeadline\":\"2025-01-20T00:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(createdCoupons.size())); // 사이즈 검증

        verify(couponAdapter, times(1)).createCoupons(eq(1), any(CouponRequestDTO.class));
    }

    @Test
    @DisplayName("POST /coupons/{couponId}/use - 쿠폰 사용")
    void testUseCoupon() throws Exception {
        // given
        Long couponId = 1L;
        CouponDTO coupon = new CouponDTO(
                couponId,
                null,
                "겨울 쿠폰",
                "회원전용",
                123L,
                ZonedDateTime.now().plusDays(30),
                ZonedDateTime.now(),
                false,
                ZonedDateTime.now()
        );
        when(couponAdapter.useCoupon(couponId)).thenReturn(coupon);

        // when & then
        mockMvc.perform(post("/coupons/{couponId}/use", couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponIsActive").value(false));

        verify(couponAdapter, times(1)).useCoupon(couponId);
    }

    @Test
    @DisplayName("POST /coupons/{couponId}/deactivate - 쿠폰 비활성화")
    void testDeactivateCoupon() throws Exception {
        // given
        Long couponId = 1L;
        Map<String, String> response = Map.of("message", "쿠폰이 비활성화되었습니다.");
        when(couponAdapter.deactivateCoupon(couponId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/coupons/{couponId}/deactivate", couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("쿠폰이 비활성화되었습니다."));

        verify(couponAdapter, times(1)).deactivateCoupon(couponId);
    }
}
