package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.point.PointPolicyDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@FeignClient(name = "hexa-gateway", contextId = "pointPolicyAdapter")
public interface PointPolicyAdapter {

    @PostMapping("/api/pointPolicies")
    ResponseEntity<PointPolicyDTO> createPointPolicy(@RequestBody @Valid PointPolicyDTO pointPolicyDTO);

    @PutMapping("/api/pointPolicies")
    ResponseEntity<PointPolicyDTO> updatePointPolicy(@RequestBody @Valid PointPolicyDTO pointPolicy);

    @GetMapping("/api/pointPolicies")
    ResponseEntity<List<PointPolicyDTO>> getAllPointPolicies();

    @DeleteMapping("/api/pointPolicies/{pointPolicyName}")
    ResponseEntity<Void> deletePointPolicy(@PathVariable String pointPolicyName);
}


