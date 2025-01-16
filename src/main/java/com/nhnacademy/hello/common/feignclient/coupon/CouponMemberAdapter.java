package com.nhnacademy.hello.common.feignclient.coupon;

import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "couponMemberAdapter")
public interface CouponMemberAdapter {

    // 특정 회원의 쿠폰 목록 조회
    @GetMapping("api/members/{memberId}/coupons")
    List<CouponMemberDTO> getMemberCoupons(@PathVariable String memberId);

    // 특정 회원에게 쿠폰 배부
    @PostMapping("api/members/{memberId}/coupons/{couponId}")
    Void createMemberCoupon(@PathVariable String memberId, @PathVariable Long couponId);

    // 회원에게서 쿠폰 회수(삭제)
    @DeleteMapping("api/members/{memberId}/coupons/{couponId}")
    Void deleteMemberCoupon(@PathVariable String memberId, @PathVariable Long couponId);

    @GetMapping("api/members/All/coupons")
    List<Long> getAllCouponId();

}
