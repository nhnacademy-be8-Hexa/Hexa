package com.nhnacademy.hello.common.feignclient.coupon;

import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "hexa-gateway", contextId = "couponAdapter")
public interface CouponAdapter {

    // 쿠폰 조회 (쿠폰 ID로 조회)
    @GetMapping("/api/coupons/{couponId}")
    CouponDTO getCouponById(@PathVariable(name = "couponId") Long couponId);

    // 모든 쿠폰 조회
    @GetMapping("/api/coupons")
    List<CouponDTO> getCouponsByActive(
            @RequestParam(required = false) List<Long> couponIds,
            @RequestParam(name = "active", required = false, defaultValue = "true") Boolean active
    );

    // 쿠폰 이름으로 조회
    @GetMapping("/api/coupons/{couponName}/name")
    List<CouponDTO> getCouponByCouponName(@PathVariable(name = "couponName") String couponName);

    // 쿠폰 생성
    @PostMapping("/api/coupons")
    List<CouponDTO> createCoupons(
            @RequestParam(value = "count", required = false, defaultValue = "1") int count,
            @RequestBody CouponRequestDTO couponDTO
    );

    // 쿠폰 사용 (쿠폰 사용 처리)
    @PostMapping("/api/coupons/{couponId}/use")
    CouponDTO useCoupon(@PathVariable(name = "couponId") Long couponId);

    // 쿠폰 비활성화 (삭제 처리)
    @PostMapping("/api/coupons/{couponId}/deactivate")
    Map<String, String> deactivateCoupon(@PathVariable(name = "couponId") Long couponId);

}
