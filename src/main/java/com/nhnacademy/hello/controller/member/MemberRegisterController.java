package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponPolicyAdapter;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponPolicyDTO;
import com.nhnacademy.hello.dto.coupon.CouponRequestDTO;
import com.nhnacademy.hello.dto.member.MemberRegisterDTO;
import com.nhnacademy.hello.dto.member.MemberRequestDTO;
import com.nhnacademy.hello.dto.point.CreatePointDetailDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class MemberRegisterController {

    private final MemberAdapter memberAdapter;
    private final PasswordEncoder passwordEncoder;
    private final CouponMemberAdapter couponMemberAdapter;
    private final CouponPolicyAdapter couponPolicyAdapter;
    private final CouponAdapter couponAdapter;
    private final PointDetailsAdapter pointDetailsAdapter;

    @GetMapping
    public String registerForm() {
        return "member/register";
    }

    @PostMapping
    public String register(@Valid MemberRegisterDTO registerDTO,
                           BindingResult bindingResult,
                           Model model) {
        // 입력 형식 검증에서 걸리면 에러 보여주고 다시
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for(ObjectError error : bindingResult.getAllErrors()) {
                errors.add(error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            model.addAttribute("registerDTO", registerDTO);
            return "member/register";
        }

        MemberRequestDTO requestDTO = new MemberRequestDTO(
                registerDTO.memberId(),
                passwordEncoder.encode(registerDTO.memberPassword()),
                registerDTO.memberName(),
                registerDTO.memberNumber(),
                registerDTO.memberEmail(),
                registerDTO.memberBirthAt(),
                null,
                "1",
                "1"
        );

        memberAdapter.createMember(requestDTO);

        try {
            // welcomeCoupon 발급
            CouponPolicyDTO couponPolicy = couponPolicyAdapter.getPolicyByEventType("welcome"); // welcome 쿠폰 정책 찾기
            CouponRequestDTO couponRequest = new CouponRequestDTO(couponPolicy.couponPolicyId(),
                    "welcome!", "ALL", null, ZonedDateTime.now().plusDays(30)); // 쿠폰 생성 값
            List<CouponDTO> coupon = couponAdapter.createCoupons(1, couponRequest); // 쿠폰 생성
            couponMemberAdapter.createMemberCoupon(registerDTO.memberId(), coupon.getFirst().couponId()); // 쿠폰 배부
        }catch (Exception e){
            log.error("쿠폰 발급 오류", e);
        }

        memberAdapter.createMember(requestDTO);

        CreatePointDetailDTO createPointDetailDTO = new CreatePointDetailDTO(
                5000,
                "회원가입 포인트 지급"
        );
        pointDetailsAdapter.createPointDetails(registerDTO.memberId(), createPointDetailDTO);



        return "redirect:/";
    }

}
