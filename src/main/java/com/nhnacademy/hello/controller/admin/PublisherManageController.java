package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.PublisherAdapter;
import com.nhnacademy.hello.dto.book.PublisherRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/publisherManage/add")
@RequiredArgsConstructor
public class PublisherManageController {
    private final PublisherAdapter publisherAdapter;

    @GetMapping
    public String publisher() {
        return "admin/publisherManage";
    }

    @PostMapping
    public String addPublisher(@ModelAttribute PublisherRequestDTO publisherRequestDTO) {
        publisherAdapter.createPublisher(publisherRequestDTO);
        return "redirect:/admin/publisherManage/add";
    }
}
