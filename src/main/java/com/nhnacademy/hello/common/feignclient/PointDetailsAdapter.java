package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.point.CreatePointDetailDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "hexa-gateway", contextId = "pointDetailsAdapter")
public interface PointDetailsAdapter {
    @PostMapping("/api/members/{memberId}/pointDetails")
     ResponseEntity<PointDetailsDTO> createPointDetails(
            @PathVariable String memberId,
            @RequestBody @Valid CreatePointDetailDTO pointDetails);


    @GetMapping("/api/members/{memberId}/pointDetails/sum")
    ResponseEntity<Long> sumPoint( @PathVariable String memberId);



    @GetMapping("/api/members/{memberId}/pointDetails")
    ResponseEntity<List<PointDetailsDTO>> getPointDetails(
            @PathVariable String memberId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort);

    @GetMapping("/api/members/{memberId}/pointDetails/count")
    ResponseEntity<Integer> countByMemberId(@PathVariable String memberId);
}
