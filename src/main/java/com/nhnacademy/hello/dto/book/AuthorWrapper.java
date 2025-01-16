package com.nhnacademy.hello.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuthorWrapper {
    private List<AuthorRequestDTO> authorRequestDTO = new ArrayList<>();
}
