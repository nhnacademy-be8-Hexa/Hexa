package com.nhnacademy.hello.controller.point;

import com.nhnacademy.hello.common.feignclient.PointPolicyAdapter;
import com.nhnacademy.hello.dto.point.PointPolicyDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pointPolicies")
public class PointPolicyController {

    private final PointPolicyAdapter pointPolicyAdapter;

    public PointPolicyController(PointPolicyAdapter pointPolicyAdapter) {
        this.pointPolicyAdapter = pointPolicyAdapter;
    }

    // 모든 포인트 정책 조회
    @GetMapping
    public ResponseEntity<List<PointPolicyDTO>> getAllPointPolicies(){
        return pointPolicyAdapter.getAllPointPolicies();
    }

    // 포인트 정책 생성
    @PostMapping
    public ResponseEntity<PointPolicyDTO> createPointPolicy(@RequestBody @Valid PointPolicyDTO pointPolicyDTO){
        return pointPolicyAdapter.createPointPolicy(pointPolicyDTO);
    }

    // 포인트 정책 수정
    @PutMapping
    public ResponseEntity<PointPolicyDTO> updatePointPolicy(@RequestBody @Valid PointPolicyDTO pointPolicyDTO){
        PointPolicyDTO updatedPolicyDTO = new PointPolicyDTO(
                pointPolicyDTO.pointPolicyName(),
                pointPolicyDTO.pointDelta()
        );
        return pointPolicyAdapter.updatePointPolicy(updatedPolicyDTO);
    }

    // 포인트 정책 삭제
    @DeleteMapping("/{pointPolicyName}")
    public ResponseEntity<Void> deletePointPolicy(@PathVariable String pointPolicyName){
        return pointPolicyAdapter.deletePointPolicy(pointPolicyName);
    }
}
