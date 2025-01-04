package com.nhnacademy.hello.controller.point;


import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.dto.point.CreatePointDetailDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/members/{memberId}/pointDetails")
@RequiredArgsConstructor
@Slf4j
public class PointDetailsController {
    private final PointDetailsAdapter pointDetailsAdapter;

    /**
     * 포인트 상세 정보 생성
     *
     * @param memberId 회원 ID
     * @param createPointDetailDTO 포인트 상세 정보
     * @return 생성된 포인트 상세 정보
     */
    @PostMapping
    public ResponseEntity<PointDetailsDTO> createPointDetails(
            @PathVariable String memberId,
            @RequestBody @Valid CreatePointDetailDTO createPointDetailDTO) {
        return pointDetailsAdapter.createPointDetails(memberId, createPointDetailDTO);
    }

    /**
     * 회원 포인트 합계 조회
     *
     * @param memberId 회원 ID
     * @return 회원 포인트 합계
     */
    @GetMapping("/sum")
    public ResponseEntity<Long> getPointSum(@PathVariable String memberId) {
        return pointDetailsAdapter.sumPoint(memberId);
    }

    /**
     * 회원 포인트 상세 정보 조회
     *
     * @param memberId 회원 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준 (예: "date,desc")
     * @return 포인트 상세 정보 목록
     */

    @GetMapping
    public ResponseEntity<List<PointDetailsDTO>> getPointDetails(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String sort) {
        ResponseEntity<List<PointDetailsDTO>> pointDetails = pointDetailsAdapter.getPointDetails(memberId, page, size, sort);
        log.warn(pointDetails.toString());
        return pointDetails;
    }
}
