package com.nhnacademy.hello.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "hexa-gateway",contextId = "memberReviewAdapter")
public interface MemberReportAdapter {

    // 회원 신고
    @PostMapping("/reports/members/{memberId}/reviews/{reviewId}")
    public ResponseEntity<Void> saveMemberReport(
            @PathVariable String memberId,
            @PathVariable Long reviewId);

    // 신고 총계
    @GetMapping("/admin/reports/reviews/{reviewId}/count")
    public ResponseEntity<Long> getTotalReport(
            @PathVariable Long reviewId);

    //관리자
    @DeleteMapping("/admin/reports/admin/reviews/{reviewId}")
    public ResponseEntity<Void> allDelete(
            @PathVariable Long reviewId);
}

