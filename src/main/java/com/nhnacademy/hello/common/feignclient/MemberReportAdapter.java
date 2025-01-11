package com.nhnacademy.hello.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "hexa-gateway", contextId = "memberReportAdapter")
public interface MemberReportAdapter {
    @PostMapping("/api/reports/members/{memberId}/reviews/{reviewId}")
    ResponseEntity<Void> saveMemberReport(
            @PathVariable("memberId") String memberId,
            @PathVariable("reviewId") Long reviewId
    );

    @GetMapping("/api/admin/reports/reviews/{reviewId}/count")
    ResponseEntity<Long> getTotalReport(
            @PathVariable("reviewId") Long reviewId
    );

    @DeleteMapping("/api/admin/reports/admin/reviews/{reviewId}")
    ResponseEntity<Void> allDelete(
            @PathVariable("reviewId") Long reviewId
    );
}
