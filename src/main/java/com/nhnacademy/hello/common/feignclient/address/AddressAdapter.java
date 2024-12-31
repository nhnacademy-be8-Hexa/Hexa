package com.nhnacademy.hello.common.feignclient.address;

import com.nhnacademy.hello.dto.address.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "addressAdapter")
public interface AddressAdapter {

    /**
     * 새로운 주소를 추가하는 메소드
     *
     * @param memberId   주소를 추가할 회원의 ID
     * @param addressDTO 추가할 주소 정보
     * @return 생성된 주소 정보
     */
    @PostMapping(value = "/api/members/{memberId}/addresses")
    AddressDTO addAddress(@PathVariable("memberId") String memberId, @RequestBody AddressDTO addressDTO);

    /**
     * 특정 회원의 주소 목록을 조회하는 메소드
     *
     * @param memberId 주소를 조회할 회원의 ID
     * @param page     페이지 번호 (0부터 시작)
     * @param size     페이지 당 항목 수
     * @param sort     정렬 기준 (예: "addressId,asc")
     * @return 주소 목록
     */
    @GetMapping(value = "/api/members/{memberId}/addresses")
    List<AddressDTO> getAddresses(@PathVariable("memberId") String memberId,
                                  @RequestParam("page") int page,
                                  @RequestParam("size") int size,
                                  @RequestParam("sort") String sort);

    /**
     * 특정 주소를 삭제하는 메소드
     *
     * @param memberId  삭제할 주소의 회원 ID
     * @param addressId 삭제할 주소의 ID
     */
    @DeleteMapping(value = "/api/members/{memberId}/addresses/{addressId}")
    void deleteAddress(@PathVariable("memberId") String memberId,
                       @PathVariable("addressId") Long addressId);
}
