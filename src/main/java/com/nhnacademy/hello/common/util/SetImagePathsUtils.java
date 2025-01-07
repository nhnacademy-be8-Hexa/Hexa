package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.image.ImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SetImagePathsUtils {
    private final ImageStore imageStore;

    /**
     * 도서 리스트에 이미지 경로를 설정하는 메소드
     */
    public List<BookDTO> setImagePaths(List<BookDTO> books) {
        return books.stream().map(book -> {
            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg"; // 기본 이미지 경로로 수정
            return new BookDTO(
                    book.bookId(),
                    book.bookTitle(),
                    book.bookDescription(),
                    book.bookPubDate(),
                    book.bookIsbn(),
                    book.bookOriginPrice(),
                    book.bookPrice(),
                    book.bookWrappable(),
                    book.bookView(),
                    book.bookAmount(),
                    book.bookSellCount(),
                    book.publisher(),
                    book.bookStatus(),
                    imagePath
            );
        }).collect(Collectors.toList());
    }
}
