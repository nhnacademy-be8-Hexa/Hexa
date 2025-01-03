package com.nhnacademy.hello.controller.purchase;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.address.AddressAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.address.AddressDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nimbusds.openid.connect.sdk.claims.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final BookAdapter bookAdapter;
    private final AddressAdapter addressAdapter;
    private final MemberAdapter memberAdapter;

    @Value("${toss.client.key}")
    private String tossClientKey;

    @GetMapping("/purchase")
    public String purchaseCartItem(
            @RequestParam List<Long> bookIds,
            @RequestParam(required = false) Integer quantity,
            Principal principal,
            Model model
    ) {

        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("bookIds 가 전달되지 않았습니다.");
        }

        // 전달받은 북 ids 로 책 리스트 받아와서 model 로 전달
        List<BookDTO> bookList = bookAdapter.getBooksByIds(bookIds);
        model.addAttribute("bookList", bookList);

        boolean isLoggedIn = AuthInfoUtils.isLogin();
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 로그인한 사용자의 배송지 목록 추가
        if (isLoggedIn) {
            String memberId = principal.getName();
            // 페이지, size, sort 파라미터 설정
            int page = 0;
            int size = 100; // 충분한 사이즈로 설정하여 모든 주소를 가져옴
            String sort = "addressId,asc";
            List<AddressDTO> addressList = addressAdapter.getAddresses(memberId, page, size, sort);
            model.addAttribute("addressList", addressList);
            model.addAttribute("member", memberAdapter.getMember(memberId));
        }

        // 바로구매로 넘어올 시 quantity가 있음
        if(quantity != null && quantity > 0) {
            model.addAttribute("buynow_quantity", quantity);
        }

        // toss client key
        model.addAttribute("clientKey", tossClientKey);

        return "purchase/purchase";
    }

    // 배송지 선택 처리
    @PostMapping("/select-address")
    @ResponseBody
    public ResponseEntity<?> selectAddress(@RequestParam Long addressId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberId = principal.getName();
        // AddressAdapter에 getAddressById 메소드가 없으므로, 모든 주소를 조회하여 해당 addressId를 찾습니다.
        List<AddressDTO> addressList = addressAdapter.getAddresses(memberId, 0, 100, "addressId,asc");
        AddressDTO selectedAddress = null;
        for (AddressDTO addr : addressList) {
            if (addr.getAddressId().equals(addressId)) {
                selectedAddress = addr;
                break;
            }
        }

        if (selectedAddress == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 주소입니다.");
        }

        // 클라이언트 측에 주소 정보 반환
        Map<String, String> addressData = new HashMap<>();
        addressData.put("recipientName", selectedAddress.getAddressName());
        addressData.put("zonecode", selectedAddress.getZoneCode());
        addressData.put("address", selectedAddress.getAddress());
        addressData.put("detailAddress", selectedAddress.getAddressDetail());

        return ResponseEntity.ok(addressData);
    }

    // 새 배송지 추가 처리
    @PostMapping("/add-address")
    @ResponseBody
    public ResponseEntity<?> addAddress(
            @RequestParam String recipientName,
            @RequestParam String zonecode,
            @RequestParam String address,
            @RequestParam String detailAddress,
            Principal principal
    ) {
        if (principal != null) {
            // 로그인된 사용자: 주소를 데이터베이스에 저장
            String memberId = principal.getName();
            AddressDTO newAddress = AddressDTO.builder()
                    .addressName(recipientName)
                    .zoneCode(zonecode)
                    .address(address)
                    .addressDetail(detailAddress)
                    .build();

            AddressDTO createdAddress = addressAdapter.addAddress(memberId, newAddress);

            // JSON 응답 반환
            Map<String, String> response = new HashMap<>();
            response.put("message", "배송지가 추가되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            // 비로그인 사용자: 주소 데이터를 클라이언트에 반환
            AddressDTO tempAddress = AddressDTO.builder()
                    .addressName(recipientName)
                    .zoneCode(zonecode)
                    .address(address)
                    .addressDetail(detailAddress)
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "배송지가 추가되었습니다.");
            response.put("address", tempAddress);
            return ResponseEntity.ok(response);
        }
    }

    // 결제 정보 처리
    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(

    ){

        return ResponseEntity.ok("결제 성공.");
    }

    // 결제 성공 페이지
    @GetMapping("/purchase/success")
    public String purchaseSuccess(

    ) {

        return "purchase/success";
    }

    //결제 실패 페이지
    @GetMapping("/purchase/fail")
    public String purchaseFail(

    ) {

        return "purchase/fail";
    }

}

