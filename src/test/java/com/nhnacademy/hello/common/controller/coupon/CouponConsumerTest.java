package com.nhnacademy.hello.common.controller.coupon;


import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.controller.coupon.CouponConsumer;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import com.nhnacademy.hello.dto.coupon.CouponPolicyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponConsumerTest {

    @Mock
    private CouponMemberAdapter couponMemberAdapter;

    @Mock
    private CouponAdapter couponAdapter;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CouponConsumer couponConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급받은 쿠폰")
    void testCouponGet_AlreadyIssuedCoupon() {
        // given
        String memberId = "testMember";
        Message message = new Message(memberId.getBytes(StandardCharsets.UTF_8));
        when(rabbitTemplate.receive("hexa.coupon.queues")).thenReturn(message);

        List<CouponMemberDTO> memberCoupons = List.of(new CouponMemberDTO(1L,2L));
        when(couponMemberAdapter.getMemberCoupons(memberId)).thenReturn(memberCoupons);

        List<CouponDTO> activeCoupons = List.of(
                new CouponDTO(
                        2L,
                        new CouponPolicyDTO(2L, "겨울 할인", 5, "겨울 시즌 할인", 10, 50, true, "겨울 프로모션", ZonedDateTime.now()),
                        "겨울쿠폰",
                        "회원전용",
                        456L,
                        ZonedDateTime.now().plusDays(30),
                        ZonedDateTime.now(),
                        true,
                        null
                )
        );
        List<CouponDTO> inactiveCoupons = List.of();
        when(couponAdapter.getCouponsByActive(anyList(), eq(true))).thenReturn(activeCoupons);
        when(couponAdapter.getCouponsByActive(anyList(), eq(false))).thenReturn(inactiveCoupons);

        List<CouponDTO> targetCoupons = List.of(
                new CouponDTO(
                        2L,
                        new CouponPolicyDTO(2L, "겨울 할인", 5, "겨울 시즌 할인", 10, 50, true, "겨울 프로모션", ZonedDateTime.now()),
                        "겨울쿠폰",
                        "회원전용",
                        456L,
                        ZonedDateTime.now().plusDays(30),
                        ZonedDateTime.now(),
                        true,
                        null
                )
        );
        when(couponAdapter.getCouponByCouponName("겨울 쿠폰")).thenReturn(targetCoupons);

        when(couponMemberAdapter.getAllCouponId()).thenReturn(List.of(1L));

        // when
        String result = couponConsumer.couponGet();

        // then
        assertEquals("이미 발급받은 쿠폰입니다.", result);
        verify(couponMemberAdapter, times(1)).getMemberCoupons(memberId);
        verify(couponAdapter, times(1)).getCouponByCouponName("겨울 쿠폰");
    }

    @Test
    @DisplayName("쿠폰 발급 - 성공")
    void testCouponGet_Success() {
        // given
        String memberId = "testMember";
        Message message = new Message(memberId.getBytes(StandardCharsets.UTF_8));
        when(rabbitTemplate.receive("hexa.coupon.queues")).thenReturn(message);

        List<CouponMemberDTO> memberCoupons = List.of();
        when(couponMemberAdapter.getMemberCoupons(memberId)).thenReturn(memberCoupons);

        List<CouponDTO> activeCoupons = List.of();
        List<CouponDTO> inactiveCoupons = List.of();
        when(couponAdapter.getCouponsByActive(anyList(), eq(true))).thenReturn(activeCoupons);
        when(couponAdapter.getCouponsByActive(anyList(), eq(false))).thenReturn(inactiveCoupons);

        List<CouponDTO> targetCoupons = List.of(
                new CouponDTO(
                        2L,
                        new CouponPolicyDTO(2L, "겨울 할인", 5, "겨울 시즌 할인", 10, 50, true, "겨울 프로모션", ZonedDateTime.now()),
                        "겨울쿠폰",
                        "회원전용",
                        456L,
                        ZonedDateTime.now().plusDays(30),
                        ZonedDateTime.now(),
                        true,
                        null
                )
        );
        when(couponAdapter.getCouponByCouponName("겨울 쿠폰")).thenReturn(targetCoupons);

        when(couponMemberAdapter.getAllCouponId()).thenReturn(List.of(1L));

        // when
        String result = couponConsumer.couponGet();

        // then
        assertEquals("쿠폰을 발급 받았습니다!", result);
        verify(couponMemberAdapter, times(1)).createMemberCoupon(memberId, 2L);
    }

    @Test
    @DisplayName("쿠폰 발급 - 실패")
    void testCouponGet_Failure() {
        // given
        String memberId = "testMember";
        Message message = new Message(memberId.getBytes(StandardCharsets.UTF_8));
        when(rabbitTemplate.receive("hexa.coupon.queues")).thenReturn(message);

        List<CouponMemberDTO> memberCoupons = List.of();
        when(couponMemberAdapter.getMemberCoupons(memberId)).thenReturn(memberCoupons);

        List<CouponDTO> activeCoupons = List.of();
        List<CouponDTO> inactiveCoupons = List.of();
        when(couponAdapter.getCouponsByActive(anyList(), eq(true))).thenReturn(activeCoupons);
        when(couponAdapter.getCouponsByActive(anyList(), eq(false))).thenReturn(inactiveCoupons);

        List<CouponDTO> targetCoupons = List.of();
        when(couponAdapter.getCouponByCouponName("겨울 쿠폰")).thenReturn(targetCoupons);

        when(couponMemberAdapter.getAllCouponId()).thenReturn(List.of());

        // when
        String result = couponConsumer.couponGet();

        // then
        assertEquals("쿠폰 발급에 실패했습니다. 잠시후에 다시 시도해주세요", result);
        verify(couponMemberAdapter, never()).createMemberCoupon(anyString(), anyLong());
    }
}
