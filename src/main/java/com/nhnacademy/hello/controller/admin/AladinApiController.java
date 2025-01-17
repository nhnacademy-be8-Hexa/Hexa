package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.AladinApiAdapter;
import com.nhnacademy.hello.dto.book.AladinBookDTO;
import com.nhnacademy.hello.dto.book.AladinBookRequestDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.image.ImageStore;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/aladinApi")
@RequiredArgsConstructor
public class AladinApiController {

    private final AladinApiAdapter aladinApiAdapter;
    private final ImageStore imageStore;

    @GetMapping
    public String searchAladinBooks(@RequestParam(required = false) String query, Model model) {
        List<AladinBookDTO> books = aladinApiAdapter.searchAladinBooks(query);
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        return "admin/aladinBookSearch";
    }

    @PostMapping
    public ResponseEntity<?> createAladinBook(@Valid @RequestBody AladinBookRequestDTO aladinBookRequestDTO) {
        BookDTO bookDTO = aladinApiAdapter.createAladinBook(aladinBookRequestDTO).getBody();
        String fileName = String.format("bookThumbnail_%d", bookDTO.bookId());
        saveImageFromUrl(aladinBookRequestDTO.cover(), fileName);
        return ResponseEntity.ok().build();
    }

    /**
     * URL을 통해 이미지를 다운로드하고 MultipartFile로 변환 후 저장.
     *
     * @param imageUrl 다운로드할 이미지 URL
     * @param fileName 저장할 파일명
     */
    public void saveImageFromUrl(String imageUrl, String fileName) {
        try {

            URI uri = new URI(imageUrl);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();

            try (InputStream inputStream = connection.getInputStream()) {

                byte[] imageBytes = IOUtils.toByteArray(inputStream);

                MultipartFile multipartFile = convertToMultipartFile(imageBytes, fileName);

                List<MultipartFile> files = new ArrayList<>();
                files.add(multipartFile);
                imageStore.saveImages(files, fileName);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * byte 배열을 MultipartFile로 변환
     *
     * @param imageBytes 이미지 바이트 배열
     * @param fileName   파일명
     * @return MultipartFile 객체
     */
    private MultipartFile convertToMultipartFile(byte[] imageBytes, String fileName) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public String getOriginalFilename() {
                return fileName + ".jpg";
            }

            @Override
            public String getContentType() {
                return "image/jpg";
            }

            @Override
            public boolean isEmpty() {
                return imageBytes.length == 0;
            }

            @Override
            public long getSize() {
                return imageBytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return imageBytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(imageBytes);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (OutputStream outStream = new FileOutputStream(dest)) {
                    outStream.write(imageBytes);
                }
            }
        };
    }
}
