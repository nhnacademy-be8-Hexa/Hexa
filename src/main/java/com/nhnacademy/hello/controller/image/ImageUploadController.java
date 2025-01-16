package com.nhnacademy.hello.controller.image;

import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.dto.image.ImageUploadResponse;
import com.nhnacademy.hello.image.ImageStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ImageUploadController {
    private final ImageStore imageStore;
    private final SetImagePathsUtils setImagePathsUtils;

    /**
     * 리뷰 이미지 업로드 엔드포인트
     * 클라이언트로부터 이미지 파일을 받아 서버에 저장하고, 이미지 URL을 반환합니다.
     *
     * @param image 업로드된 이미지 파일
     * @return 이미지 URL을 포함한 JSON 응답
     */
    @PostMapping("/upload-review-image")
    @ResponseBody
    public ResponseEntity<ImageUploadResponse> uploadReviewImage(@RequestParam("image") MultipartFile image) {
        // 1. 파일이 비어있는지 확인
        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2. 허용된 이미지 타입 리스트
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/gif");

        if (!allowedTypes.contains(image.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 3. 현재 사용자 ID 가져오기
        String memberId = AuthInfoUtils.getUsername();
        if (memberId == null || memberId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 4. 고유한 파일 이름 생성 (UUID 사용)
            String originalFilename = Objects.requireNonNull(image.getOriginalFilename());

            String uniqueFilename = String.valueOf(UUID.randomUUID());

            // 5. 이미지 저장 로직 (ImageStore를 활용)
            // 파일 이름에 사용자 ID를 포함하여 폴더 구분
            String fileNameForSave = "review-images-" + memberId + "-" + uniqueFilename;
            boolean saveSuccess = imageStore.saveImages(Collections.singletonList(image), fileNameForSave);

            if (!saveSuccess) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 7. 성공 응답 반환
            return ResponseEntity.ok(new ImageUploadResponse(imageStore.getImage(fileNameForSave).getFirst()));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
