package com.nhnacademy.hello.controller.tag;

import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.dto.tag.TagRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagController {
    private final TagAdapter tagAdapter;
    private final BooKTagAdapter booKTagAdapter;

    @GetMapping("/tags")
    @ResponseBody
    public ResponseEntity<List<TagDTO>> listTags() {
        ResponseEntity<List<TagDTO>> response = tagAdapter.getAllTags();
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<TagDTO> createTagAjax(@Valid @RequestBody TagRequestDTO tagRequestDTO) {
        ResponseEntity<TagDTO> response = tagAdapter.createTag(tagRequestDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(response.getBody(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
