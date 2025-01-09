package com.nhnacademy.hello.controller.tag;

import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.dto.tag.TagRequestDTO;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BookTagController {
    private final TagAdapter tagAdapter;
    private final BooKTagAdapter bookTagAdapter;


    @GetMapping("/book/{bookId}/admin/tagSelect")
    public String selectTagForm(@PathVariable("bookId") Long bookId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int pageSize,
                                Model model) {
        List<TagDTO> tags = tagAdapter.getAllTags(page,pageSize, "").getBody();

        List<TagDTO> assignedTags = bookTagAdapter.getTagsByBook(bookId).getBody();
        if (assignedTags == null) {
            assignedTags = List.of();
        }

        // 태그 총계 가져오기 (필터링 조건을 반영)
        Long totalTags = tagAdapter.getTotalTags().getBody();

        // 전체 페이지 수 계산 (size가 9인 것을 전제로 함)
        int totalPages = (int) Math.ceil((double) totalTags / pageSize);


        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);


        model.addAttribute("assignedTags", assignedTags);
        model.addAttribute("bookId", bookId);
        model.addAttribute("tags", tags);
        model.addAttribute("bookId", bookId);

        return "admin/tagSelect";
    }

    @PostMapping("/book/{bookId}/admin/tagSelect")
    public String assignTagsToBook(@PathVariable("bookId") Long bookId,
                                   @RequestParam(value = "selectedTags", required = false) List<Long> selectedTagIds,
                                   Model model) {
        if (selectedTagIds == null) {
            selectedTagIds = List.of();
        }

        // 현재 책에 할당된 태그 가져오기
        List<TagDTO> currentTags = bookTagAdapter.getTagsByBook(bookId).getBody();
        if (currentTags == null) {
            currentTags = List.of();
        }
        Set<Long> currentTagIds = currentTags.stream()
                .map(TagDTO::getTagId)
                .collect(Collectors.toSet());

        Set<Long> selectedTagIdSet = new HashSet<>(selectedTagIds);

        // 추가할 태그: 선택된 태그 중 현재 할당되지 않은 태그
        Set<Long> tagsToAdd = new HashSet<>(selectedTagIdSet);
        tagsToAdd.removeAll(currentTagIds);

        // 제거할 태그: 현재 할당된 태그 중 선택되지 않은 태그
        Set<Long> tagsToRemove = new HashSet<>(currentTagIds);
        tagsToRemove.removeAll(selectedTagIdSet);

        List<String> conflictTags = new ArrayList<>();
        List<String> removedTags = new ArrayList<>();

        // 태그 추가
        for (Long tagId : tagsToAdd) {
            try {
                bookTagAdapter.addBookTag(bookId, tagId);
            } catch (FeignException.Conflict e) {
                // 이미 태그가 할당된 경우
                conflictTags.add(String.valueOf(tagId));
            } catch (FeignException.NotFound e) {
                // 태그나 책이 존재하지 않는 경우
                model.addAttribute("error", "태그 ID " + tagId + " 또는 책 ID " + bookId + "가 존재하지 않습니다.");
                return "admin/tagSelect";
            }
        }

        // 태그 제거
        for (Long tagId : tagsToRemove) {
            try {
                bookTagAdapter.deleteBookTag(bookId, tagId);
                removedTags.add(String.valueOf(tagId));
            } catch (FeignException.NotFound e) {
                // 태그나 책이 존재하지 않는 경우
                model.addAttribute("error", "태그 ID " + tagId + " 또는 책 ID " + bookId + "가 존재하지 않습니다.");
                return "admin/tagSelect";
            }
        }

        // 사용자에게 피드백 제공
        if (!conflictTags.isEmpty() || !removedTags.isEmpty()) {
            StringBuilder message = new StringBuilder();
            if (!conflictTags.isEmpty()) {
                message.append("이미 할당된 태그 ID: ").append(String.join(", ", conflictTags)).append(". ");
            }
            if (!removedTags.isEmpty()) {
                message.append("제거된 태그 ID: ").append(String.join(", ", removedTags)).append(". ");
            }
            model.addAttribute("message", message.toString());
        }

        return "redirect:/book/" + bookId;
    }


}
