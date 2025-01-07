package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.common.feignclient.address.AddressAdapter;
import com.nhnacademy.hello.common.feignclient.auth.TokenAdapter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.common.util.CookieUtil;
import com.nhnacademy.hello.dto.address.AddressDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MemberAdapter memberAdapter;
    private final AddressAdapter addressAdapter;
    private final PointDetailsAdapter pointDetailsAdapter;
    private final TokenAdapter tokenAdapter;
    private final JwtProperties jwtProperties;

    @GetMapping("/mypage")
    public String myPage(
            Model model
    ) {

        // 먼저 로그인했는지 검사
        if(!AuthInfoUtils.isLogin()){
            // 로그인 하지 않았을 경우, 로그인 화면으로 이동
            return "redirect:/login";
        }

        // 현재 로그인된 아이디를 이용해서 api 로부터 멤버 정보 받아옴
        MemberDTO member = memberAdapter.getMember(AuthInfoUtils.getUsername());
        model.addAttribute("member", member);

        return "member/myPage";
    }

    @GetMapping("/mypage/edit")
    public String editPage(
            Model model
    ){
        MemberDTO member = memberAdapter.getMember(AuthInfoUtils.getUsername());
        MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                null,
                member.memberName(),
                member.memberNumber(),
                member.memberEmail(),
                member.memberBirthAt(),
                null,
                null
        );

        model.addAttribute("updateDTO", updateDTO);

        return "member/edit";
    }

    @PostMapping("/mypage/edit")
    public String edit(
        @Valid MemberUpdateDTO updateDTO,
        BindingResult bindingResult,
        Model model
    ){
        // 입력 형식 검증에서 걸리면 에러 보여주고 다시
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for(ObjectError error : bindingResult.getAllErrors()) {
                errors.add(error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            model.addAttribute("updateDTO", updateDTO);
            return "member/edit";
        }

        memberAdapter.updateMember(AuthInfoUtils.getUsername(), updateDTO);

        return "redirect:/mypage";
    }

    /**
     * 주소 입력 폼을 보여주는 메소드
     *
     * @param model 뷰에 전달할 모델
     * @return 주소 입력 폼 뷰
     */
    @GetMapping("/mypage/address/form")
    public String showAddressForm(Model model) {
        String memberId = AuthInfoUtils.getUsername();
        model.addAttribute("addressDTO", new AddressDTO());
        model.addAttribute("memberId", memberId);
        return "member/addressForm"; // resources/templates/member/addressForm.html
    }

    /**
     * 주소 입력 폼 제출을 처리하는 메소드
     *
     * @param addressDTO        제출된 주소 정보
     * @param bindingResult     바인딩 결과
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 주소 목록 페이지로 리다이렉트
     */
    @PostMapping("/mypage/address")
    public String submitAddressForm(@Valid @ModelAttribute("addressDTO") AddressDTO addressDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        String memberId = AuthInfoUtils.getUsername();

        if (bindingResult.hasErrors()) {
            return "member/addressForm";
        }

        try {
            addressAdapter.addAddress(memberId, addressDTO);
            redirectAttributes.addFlashAttribute("successMessage", "주소가 성공적으로 추가되었습니다.");
            return "redirect:/mypage/address";
        } catch (FeignException e) {
            // 에러 처리 로직 (예: 에러 메시지 추가)
            bindingResult.reject("error.addAddress", "주소 추가 중 오류가 발생했습니다.");
            return "member/addressForm";
        }
    }

    /**
     * 주소 목록을 보여주는 메소드
     *
     * @param model 뷰에 전달할 모델
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 주소 목록 뷰
     */
    @GetMapping("/mypage/address")
    public String listAddresses(Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                @RequestParam(value = "sort", required = false) String sort) {
        String memberId = AuthInfoUtils.getUsername();

        try {
            List<AddressDTO> addresses = addressAdapter.getAddresses(memberId, page, size, sort);
            model.addAttribute("addresses", addresses);
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "주소 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("addresses", Collections.emptyList()); // 빈 리스트 설정
        }
        return "member/addressList"; // templates/member/addressList.html
    }

    /**
     * 특정 주소를 삭제하는 메소드
     *
     * @param addressId         삭제할 주소 ID
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 주소 목록 페이지로 리다이렉트
     */
    @PostMapping("/mypage/address/{addressId}/delete")
    public String deleteAddress(@PathVariable("addressId") Long addressId,
                                RedirectAttributes redirectAttributes) {
        String memberId = AuthInfoUtils.getUsername();

        try {
            addressAdapter.deleteAddress(memberId, addressId);
            redirectAttributes.addFlashAttribute("successMessage", "주소가 성공적으로 삭제되었습니다.");
        } catch (FeignException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "주소 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage/address";
    }

    @GetMapping("/mypage/couponList")
    public String listCoupon(Model model){

        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        model.addAttribute("member", memberDTO);

        return "member/couponList";
    }

    @GetMapping("/mypage/delete")
    public String memberDelete(HttpServletRequest request , HttpServletResponse response) {
        MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                "3"
        );
        // 멤버 상태를 '탈퇴'로 업데이트
        memberAdapter.updateMember(AuthInfoUtils.getUsername(), updateDTO);
        String refreshToken = CookieUtil.getCookieValue(request,"refreshToken");
        tokenAdapter.deleteToken(jwtProperties.getTokenPrefix() + " " + refreshToken);
        return "redirect:/";
    }

}
