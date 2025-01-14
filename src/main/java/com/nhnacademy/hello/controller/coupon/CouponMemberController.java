package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
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
    public List<CouponMemberDTO> getMemberCoupons(@PathVariable String memberId) {
        return couponMemberAdapter.getMemberCoupons(memberId);
    }

    @PostMapping("/{memberId}/coupons/{couponId}")
    public Void createMemberCoupon(
            @PathVariable String memberId,
            @PathVariable Long couponId) {
        return couponMemberAdapter.createMemberCoupon(memberId, couponId);
    }

    @DeleteMapping("/{memberId}/coupons/{couponId}")
    public Void deleteMemberCoupon(@PathVariable String memberId, @PathVariable Long couponId) {
        return couponMemberAdapter.deleteMemberCoupon(memberId, couponId);
    }

}
