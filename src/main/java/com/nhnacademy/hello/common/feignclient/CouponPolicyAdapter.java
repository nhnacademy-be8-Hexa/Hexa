package com.nhnacademy.hello.common.feignclient;


import com.nhnacademy.hello.dto.coupon.CouponPolicyDTO;
import com.nhnacademy.hello.dto.coupon.CouponPolicyRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "hexa-gateway", contextId = "couponPolicyAdapter")
public interface CouponPolicyAdapter {

    // 모든 쿠폰 정책 조회
    @GetMapping("/api/policies")
    List<CouponPolicyDTO> getPolicies(
            @RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted
    );

    // 특정 쿠폰 정책 조회
    @GetMapping("/api/policies/{policyId}")
    CouponPolicyDTO getPolicyById(@PathVariable(name = "policyId") Long policyId);

    // 특정 이벤트 타입의 쿠폰 정책 조회
    @GetMapping("/api/policies/{eventType}/eventType")
    CouponPolicyDTO getPolicyByEventType(
            @PathVariable("eventType") String eventType
    );

    // 쿠폰 정책 생성
    @PostMapping("/api/policies")
    CouponPolicyDTO createPolicy(@RequestBody CouponPolicyRequestDTO couponPolicy);

    // 쿠폰 정책 수정
    @PatchMapping("/api/policies/{policyId}")
    CouponPolicyDTO updatePolicy(
            @PathVariable(name = "policyId") Long policyId,
            @RequestBody CouponPolicyRequestDTO updatedPolicy
    );

    // 쿠폰 정책 삭제
    @DeleteMapping("/api/policies/{policyId}")
    Map<String, String> deletePolicy(@PathVariable(name = "policyId") Long policyId);

}
