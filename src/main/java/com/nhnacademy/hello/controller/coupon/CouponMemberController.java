package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class CouponMemberController {

    private final CouponMemberAdapter couponMemberAdapter;

    public CouponMemberController(CouponMemberAdapter couponMemberAdapter) {
        this.couponMemberAdapter = couponMemberAdapter;
    }

    @GetMapping("/{memberId}/coupons")
    public List<CouponMemberDTO> getMemberCoupons(@PathVariable String memberId, HttpServletRequest request) {
        return couponMemberAdapter.getMemberCoupons(memberId, request);
    }

    @PostMapping("//{memberId}/coupons/{couponId}")
    public Void createMemberCoupon(
            @PathVariable String memberId,
            @PathVariable Long couponId, HttpServletRequest request) {
        return couponMemberAdapter.createMemberCoupon(memberId, couponId, request);
    }

    @DeleteMapping("/{memberId}/coupons/{couponId}")
    public Void deleteMemberCoupon(@PathVariable String memberId, @PathVariable Long couponId, HttpServletRequest request) {
        return couponMemberAdapter.deleteMemberCoupon(memberId, couponId, request);
    }
}
