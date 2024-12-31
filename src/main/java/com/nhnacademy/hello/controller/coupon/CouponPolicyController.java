package com.nhnacademy.hello.controller.coupon;


import com.nhnacademy.hello.common.feignclient.coupon.CouponPolicyAdapter;
import com.nhnacademy.hello.dto.coupon.CouponPolicyDTO;
import com.nhnacademy.hello.dto.coupon.CouponPolicyRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon-policies")
public class CouponPolicyController {

    private final CouponPolicyAdapter couponPolicyAdapter;

    public CouponPolicyController(CouponPolicyAdapter couponPolicyAdapter) {
        this.couponPolicyAdapter = couponPolicyAdapter;
    }

    // 모든 쿠폰 정책 조회
    @GetMapping
    public List<CouponPolicyDTO> getPolicies(
            @RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted
    ){
        return couponPolicyAdapter.getPolicies(deleted);
    }

    // 특정 쿠폰 정책 조회
    @GetMapping("/{policyId}")
    public CouponPolicyDTO getPolicyById(@PathVariable(name = "policyId") Long policyId) {
        return couponPolicyAdapter.getPolicyById(policyId);
    }

    // 특정 이벤트 타입의 쿠폰 정책 조회
    @GetMapping("/{eventType}/eventType")
    public CouponPolicyDTO getPolicyByEventType(
            @PathVariable("eventType") String eventType
    ){
        return couponPolicyAdapter.getPolicyByEventType(eventType);
    }

    // 쿠폰 정책 생성
    @PostMapping
    public CouponPolicyDTO createPolicy(@RequestBody @Valid CouponPolicyRequestDTO couponPolicy) {
        return couponPolicyAdapter.createPolicy(couponPolicy);
    }

    // 쿠폰 정책 수정
    @PutMapping("/{policyId}")
    public CouponPolicyDTO updatePolicy(
            @PathVariable(name = "policyId") Long policyId,
            @RequestBody @Valid CouponPolicyRequestDTO updatedPolicy
    ) {
        return couponPolicyAdapter.updatePolicy(policyId, updatedPolicy);
    }

    // 쿠폰 정책 삭제
    @DeleteMapping("/{policyId}")
    public void  deletePolicy(@PathVariable(name = "policyId") Long policyId) {
        couponPolicyAdapter.deletePolicy(policyId);
    }

}
