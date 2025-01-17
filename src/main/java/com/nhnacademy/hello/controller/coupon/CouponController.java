package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponRequestDTO;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponAdapter couponAdapter;

    public CouponController(CouponAdapter couponAdapter) {
        this.couponAdapter = couponAdapter;
    }

    // 쿠폰 조회 (ID로 조회)
    @GetMapping("/{couponId}")
    public CouponDTO getCouponById(@PathVariable Long couponId) {
        return couponAdapter.getCouponById(couponId);
    }

    // 모든 쿠폰 조회 & 유저가 가진 쿠폰들 조회
    @GetMapping
    public List<CouponDTO> getCouponsByActive(
            @RequestParam(required = false) List<Long> couponIds,
            @RequestParam(name = "active", required = false, defaultValue = "true") Boolean active) {
        return couponAdapter.getCouponsByActive(couponIds, active);
    }

    // 쿠폰 생성
    @PostMapping
    public List<CouponDTO> createCoupons(
            @RequestParam(value = "count", required = false, defaultValue = "1") int count,
            @RequestBody CouponRequestDTO couponRequestDTO) {
        return couponAdapter.createCoupons(count, couponRequestDTO);
    }

    // 쿠폰 사용
    @PostMapping("/{couponId}/use")
    public CouponDTO useCoupon(@PathVariable Long couponId) {
        return couponAdapter.useCoupon(couponId);
    }

    // 쿠폰 비활성화
    @PostMapping("/{couponId}/deactivate")
    public Map<String, String> deactivateCoupon(@PathVariable Long couponId) {
        return couponAdapter.deactivateCoupon(couponId);
    }
}
