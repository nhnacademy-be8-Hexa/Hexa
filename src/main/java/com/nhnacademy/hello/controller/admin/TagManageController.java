package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.dto.tag.TagRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TagManageController {

    private final MemberAdapter memberAdapter;
    private final TagAdapter tagAdapter;

    //태그 관리 페이지
    @GetMapping("/admin/tagManage")
    public String tagList(
            // 페이징 파라미터 (size는 고정값 9)
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model ) {

        // 로그인 검사
        if(!AuthInfoUtils.isLogin()){
            return "redirect:/login";
        }

        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        // 관리자인지 검사
        if(!"ADMIN".equals(memberDTO.memberRole())){
            return "redirect:/index";
        }

        // 태그 목록과 페이징 정보 조회
        // 태그 목록과 페이징 정보 조회
        List<TagDTO> tags = tagAdapter.getAllTags(page-1, pageSize, "").getBody();
        model.addAttribute("tags", tags);



        // 태그 총계 가져오기 (필터링 조건을 반영)
        Long totalTags = tagAdapter.getTotalTags().getBody();

        // 전체 페이지 수 계산 (size가 9인 것을 전제로 함)
        int totalPages = (int) Math.ceil((double) totalTags / pageSize);


        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);



        return "admin/tagManage";
    }

    //태그 추가 폼
    @GetMapping("/admin/tagManage/add")
    public String showAddTagForm(Model model) {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        model.addAttribute("tagRequestDTO", tagRequestDTO);



        return "admin/tagCreateForm"; // Thymeleaf 템플릿 경로
    }

    //태그 추가
    @PostMapping("/admin/tagManage/add")
    public String addTag(@Valid @ModelAttribute("tagRequestDTO") TagRequestDTO tagRequestDTO, Model model) {
        ResponseEntity<TagDTO> response = tagAdapter.createTag(tagRequestDTO);
        if(response.getStatusCode().is2xxSuccessful()){
            return "redirect:/admin/tagManage";
        }

        return "admin/tagCreateForm";
    }

    @GetMapping("/admin/tagManage/edit/{tagId}")
    public String showEditTagForm(@PathVariable("tagId") Long tagId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  Model model) {

        TagRequestDTO tagRequestDTO = null;
        int size = 9;
        TagDTO tag = tagAdapter.getTagById(tagId).getBody();


            if(tag.tagId().equals(tagId)){
                tagRequestDTO = new TagRequestDTO(
                        tag.tagName()
                );
            }


        model.addAttribute("tagRequestDTO", tagRequestDTO);
        return "admin/tagEditForm";
    }

    @PostMapping("/admin/tagManage/edit/{tagId}")
    public String updateTag(@PathVariable Long tagId,
                            @Valid @ModelAttribute("tagRequestDTO") TagRequestDTO tagRequestDTO,
                            Model model) {
        tagAdapter.updateTag(tagRequestDTO,tagId);
        return "redirect:/admin/tagManage";

    }

    @PostMapping("/admin/tagManage/delete/{tagId}")
    public String deleteTag(@PathVariable Long tagId, Model model) {
        tagAdapter.deleteTag(tagId);
        return "redirect:/admin/tagManage";
    }




}
