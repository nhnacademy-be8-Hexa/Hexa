package com.nhnacademy.hello.controller.purchase;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.feignclient.address.AddressAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.address.AddressDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import com.nhnacademy.hello.dto.order.WrappingPaperDTO;
import com.nhnacademy.hello.dto.purchase.PurchaseBookDTO;
import com.nhnacademy.hello.dto.purchase.PurchaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final BookAdapter bookAdapter;
    private final AddressAdapter addressAdapter;
    private final MemberAdapter memberAdapter;

    // 주문 정보
    private final OrderAdapter orderAdapter;
    private final WrappingPaperAdapter wrappingPaperAdapter;
    private final OrderStatusAdapter orderStatusAdapter;

    @Value("${toss.client.key}")
    private String tossClientKey;

    @Value("${toss.secret.key}")
    private String tossSecretKey;

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

        // 포장지 목록 전달
        List<WrappingPaperDTO> wrappingPaperList = wrappingPaperAdapter.getAllWrappingPapers();
        model.addAttribute("wrappingPaperList", wrappingPaperList);

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

    // 결제 정보 저장
    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(
            @RequestBody PurchaseDTO purchaseDTO
    ){

        // toss에 결제 승인 요청
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", "Basic " + tossSecretKey)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\"paymentKey\":\"" + purchaseDTO.paymentKey() + "\",\"orderId\":\"" + purchaseDTO.orderId() + "\",\"amount\":" + purchaseDTO.amount() + "}"))
                .build();

        try {
            HttpResponse<?> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // 성공적인 응답
            if (response.statusCode() != 200) {
                // 토스에서 반환한 에러 메시지
                return ResponseEntity
                        .status(response.statusCode())
                        .body(response.body());
            }

        } catch (IOException | InterruptedException e) {
            // 시스템 예외
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 처리 중 내부 오류가 발생했습니다.");
        }

        // 'WAIT' 주문 상태의 아이디 검색
        Long statusId = 1L;
        for(OrderStatusDTO dto : orderStatusAdapter.getAllOrderStatus()){
            if(dto.orderStatus().equals("WAIT")){
                statusId = dto.orderStatusId();
                break;
            }
        }

        // order 저장
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                AuthInfoUtils.isLogin()? AuthInfoUtils.getUsername() : null,
                purchaseDTO.amount(),
                null,
                statusId,
                purchaseDTO.zoneCode(),
                purchaseDTO.address(),
                purchaseDTO.addressDetail()
        );

        orderAdapter.createOrder(
                orderRequestDTO,
                purchaseDTO.books().stream().map(PurchaseBookDTO::bookId).toList(),
                purchaseDTO.books().stream().map(PurchaseBookDTO::quantity).toList(),
                null
                );

        return ResponseEntity.ok("결제 성공.");
    }

    // 결제 성공 페이지
    @GetMapping("/purchase/success")
    public String purchaseSuccess(
            @RequestParam("orderName") String orderName,
            @RequestParam("amount") Double amount,
            Model model
    ) {
        model.addAttribute("orderName", orderName);
        model.addAttribute("totalPrice", new DecimalFormat("#,###").format(amount));
        return "purchase/success";
    }

    //결제 실패 페이지
    @GetMapping("/purchase/fail")
    public String purchaseFail(
            @RequestParam("errorCode") String errorCode,
            @RequestParam("errorMessage") String errorMessage,
            Model model
    ) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMessage", errorMessage);
        return "purchase/fail";
    }

}

