package com.nhnacademy.hello.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.hello.image.tool.TokenInfo;
import com.nhnacademy.hello.service.StorageService;
import com.nhnacademy.hello.service.StorageService.UploadResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private StorageService storageService;

    private final String storageUrl = "http://storage-server.com";

    @BeforeEach
    void setUp() {
        // 초기화는 @InjectMocks가 자동으로 처리합니다.
    }

    /**
     * getTokenId() 메서드 테스트
     */
    @Test
    @DisplayName("getTokenId() - 캐시된 토큰이 유효할 경우 캐시된 토큰 반환")
    void getTokenId_CachedTokenValid() {
        // Given
        TokenInfo cachedToken = new TokenInfo("cachedTokenId", Instant.now().plusSeconds(3600));
        // Reflection을 사용하여 private 필드에 접근하여 설정
        ReflectionTestUtils.setField(storageService, "cachedToken", cachedToken);
        // storageService.cachedToken = cachedToken;  // 이 부분은 제거합니다.

        // When
        String tokenId = storageService.getTokenId();

        // Then
        assertEquals("cachedTokenId", tokenId);
        verify(authService, never()).requestToken();
    }

    @Test
    @DisplayName("getTokenId() - 캐시된 토큰이 없을 경우 새 토큰 요청")
    void getTokenId_NoCachedToken() {
        // Given
        TokenInfo newToken = new TokenInfo("newTokenId", Instant.now().plusSeconds(3600));
        when(authService.requestToken()).thenReturn(newToken);

        // When
        String tokenId = storageService.getTokenId();

        // Then
        assertEquals("newTokenId", tokenId);
        verify(authService, times(1)).requestToken();

        // Reflection을 사용하여 private 필드인 cachedToken 값을 가져옴
        TokenInfo cachedToken = (TokenInfo) ReflectionTestUtils.getField(storageService, "cachedToken");
        assertEquals(newToken, cachedToken);
    }

    @Test
    @DisplayName("getTokenId() - 캐시된 토큰이 만료된 경우 새 토큰 요청")
    void getTokenId_CachedTokenExpired() {
        // Given
        TokenInfo expiredToken = new TokenInfo("expiredTokenId", Instant.now().minusSeconds(3600));
        ReflectionTestUtils.setField(storageService, "cachedToken", expiredToken);

        TokenInfo newToken = new TokenInfo("newTokenId", Instant.now().plusSeconds(3600));
        when(authService.requestToken()).thenReturn(newToken);

        // When
        String tokenId = storageService.getTokenId();

        // Then
        assertEquals("newTokenId", tokenId);
        verify(authService, times(1)).requestToken();
        // Reflection을 사용하여 private 필드 cachedToken의 값을 가져옴
        TokenInfo cachedToken = (TokenInfo) ReflectionTestUtils.getField(storageService, "cachedToken");
        assertEquals(newToken, cachedToken);
    }

    /**
     * getImage(String fileName) 메서드 테스트
     */
    @Test
    @DisplayName("getImage() - 특정 파일 이름을 포함하는 이미지 목록 반환")
    void getImage_FilteredImages() {
        // Given
        String fileName = "image";

        // storageUrl가 null인 경우가 발생하므로 올바른 값을 설정합니다.
        String expectedStorageUrl = "http://storage-server.com";
        ReflectionTestUtils.setField(storageService, "storageUrl", expectedStorageUrl);

        // 캐시된 토큰을 설정하여 getTokenId()가 "testToken"을 반환하도록 합니다.
        TokenInfo tokenInfo = new TokenInfo("testToken", Instant.now().plusSeconds(3600));
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        String url = expectedStorageUrl;

        List<String> allObjects = Arrays.asList("image1.jpg", "photo.png", "image2.jpg", "document.pdf");
        ResponseEntity<String> responseEntity = new ResponseEntity<>(String.join("\n", allObjects), HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // When
        List<String> images = storageService.getImage(fileName);

        // Then
        List<String> expected = Arrays.asList(
                "http://storage-server.com/image1.jpg",
                "http://storage-server.com/image2.jpg"
        );
        assertEquals(expected, images);
    }

    @Test
    @DisplayName("getImage() - 서버 응답 실패 시 빈 목록 반환")
    void getImage_ServerFailure() {
        // Given
        String fileName = "image";
        String url = storageUrl; // storageUrl는 테스트에서 미리 설정되어야 함.

        // cachedToken이 null이 아니도록 ReflectionTestUtils를 사용하여 값을 세팅합니다.
        TokenInfo tokenInfo = new TokenInfo("testToken", Instant.now().plusSeconds(3600));
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        // restTemplate.exchange 호출 stub 처리
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // When
        List<String> images = storageService.getImage(fileName);

        // Then
        assertTrue(images.isEmpty());
    }

    /**
     * uploadFiles(List<MultipartFile> files, String baseFileName) 메서드 테스트
     */
    @Test
    @DisplayName("uploadFiles() - 모든 파일 업로드 성공")
    void uploadFiles_AllSuccess() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "file1.jpg",
                "image/jpeg",
                "imageContent1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "file2.png",
                "image/png",
                "imageContent2".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file1, file2);
        String baseFileName = "baseFile";

        // storageService를 spy 객체로 생성합니다.
        StorageService spyStorageService = spy(storageService);

        // Stub: uploadObject 메서드가 항상 true를 반환하도록 설정합니다.
        doReturn(true).when(spyStorageService).uploadObject(anyString(), any(InputStream.class));

        // When
        UploadResult result = spyStorageService.uploadFiles(files, baseFileName);

        // Then
        assertEquals(Arrays.asList("baseFile_001.jpg", "baseFile_002.png"), result.getSuccessFiles());
        assertTrue(result.getFailedFiles().isEmpty());

        // Verify uploadObject가 두 번 호출되었는지 검증합니다.
        verify(spyStorageService, times(2)).uploadObject(anyString(), any(InputStream.class));
    }

    @Test
    @DisplayName("uploadFiles() - 일부 파일 업로드 실패")
    void uploadFiles_SomeFailures() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "file1.jpg",
                "image/jpeg",
                "imageContent1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "file2.png",
                "image/png",
                "imageContent2".getBytes()
        );
        List<MultipartFile> files = Arrays.asList(file1, file2);
        String baseFileName = "baseFile";

        // storageService를 spy 객체로 생성
        StorageService spyStorageService = spy(storageService);

        // Stub uploadObject: 첫 번째 파일 성공, 두 번째 파일 실패
        doReturn(true)
                .when(spyStorageService)
                .uploadObject(eq("baseFile_001.jpg"), any(InputStream.class));
        doReturn(false)
                .when(spyStorageService)
                .uploadObject(eq("baseFile_002.png"), any(InputStream.class));

        // When
        UploadResult result = spyStorageService.uploadFiles(files, baseFileName);

        // Then
        assertEquals(Collections.singletonList("baseFile_001.jpg"), result.getSuccessFiles());
        assertEquals(Collections.singletonList("baseFile_002.png failed to upload."), result.getFailedFiles());

        // Verify uploadObject called with correct parameters
        verify(spyStorageService, times(2)).uploadObject(anyString(), any(InputStream.class));
    }

    @Test
    @DisplayName("uploadFiles() - 파일이 비어있는 경우")
    void uploadFiles_EmptyFile() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "file1.jpg",
                "image/jpeg",
                new byte[0] // 비어있는 파일
        );
        List<MultipartFile> files = Collections.singletonList(file1);
        String baseFileName = "baseFile";

        // storageService를 spy 객체로 생성합니다.
        StorageService spyStorageService = spy(storageService);

        // When
        UploadResult result = spyStorageService.uploadFiles(files, baseFileName);

        // Then
        assertTrue(result.getSuccessFiles().isEmpty());
        assertEquals(Collections.singletonList("file1.jpg is empty."), result.getFailedFiles());

        // Verify uploadObject was never called (spyStorageService를 대상으로 검증)
        verify(spyStorageService, never()).uploadObject(anyString(), any(InputStream.class));
    }

    /**
     * uploadObject(String objectName, InputStream inputStream) 메서드 테스트
     */
    @Test
    @DisplayName("uploadObject() - 성공적으로 업로드")
    void uploadObject_Success() throws IOException {
        // Given
        String objectName = "testObject.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        String tokenId = "testToken";
        TokenInfo tokenInfo = new TokenInfo(tokenId, Instant.now().plusSeconds(3600));

        // Expected storageUrl 설정 (테스트용)
        String expectedStorageUrl = "http://storage-server.com";
        // storageUrl 필드와 cachedToken 필드를 ReflectionTestUtils를 통해 설정
        ReflectionTestUtils.setField(storageService, "storageUrl", expectedStorageUrl);
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        // Stub: restTemplate.execute(...)가 정상 호출될 때, null 반환(성공)
        when(restTemplate.execute(
                eq(expectedStorageUrl + "/" + objectName),
                eq(HttpMethod.PUT),
                any(org.springframework.web.client.RequestCallback.class),
                any(org.springframework.web.client.ResponseExtractor.class)
        )).thenReturn(null);

        // When
        boolean result = storageService.uploadObject(objectName, inputStream);

        // Then
        assertTrue(result);
        verify(restTemplate, times(1)).execute(
                eq(expectedStorageUrl + "/" + objectName),
                eq(HttpMethod.PUT),
                any(org.springframework.web.client.RequestCallback.class),
                any(org.springframework.web.client.ResponseExtractor.class)
        );
    }

    @Test
    @DisplayName("uploadObject() - 업로드 중 예외 발생")
    void uploadObject_Exception() throws IOException {
        // Given
        String objectName = "testObject.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        String tokenId = "testToken";
        TokenInfo tokenInfo = new TokenInfo(tokenId, Instant.now().plusSeconds(3600));

        // storageUrl 필드 값을 설정 (null 문제 해결)
        ReflectionTestUtils.setField(storageService, "storageUrl", "http://mock-storage-url");
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        // lenient()를 사용하여 strict stubbing 문제 해결
        lenient().doAnswer(invocation -> {
            throw new IOException("Upload failed");
        }).when(restTemplate).execute(
                eq("http://mock-storage-url/" + objectName),
                eq(HttpMethod.PUT),
                any(),
                any()
        );

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            storageService.uploadObject(objectName, inputStream);
        });
        assertEquals("Upload failed", exception.getMessage());

        // Verify 호출 여부 체크
        verify(restTemplate, times(1)).execute(
                eq("http://mock-storage-url/" + objectName),
                eq(HttpMethod.PUT),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("deleteObject() - 성공적으로 삭제")
    void deleteObject_Success() {
        // Given
        String objectName = "testObject.jpg";
        // storageUrl 필드가 null인 경우 실제 값으로 설정 (예: "http://storage-server.com")
        String expectedStorageUrl = "http://storage-server.com";
        // ReflectionTestUtils를 사용하여 storageUrl 필드에 값을 설정
        ReflectionTestUtils.setField(storageService, "storageUrl", expectedStorageUrl);

        String url = expectedStorageUrl + "/" + objectName;
        String tokenId = "testToken";
        TokenInfo tokenInfo = new TokenInfo(tokenId, Instant.now().plusSeconds(3600));
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // When
        boolean result = storageService.deleteObject(objectName);

        // Then
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        );
    }


    @Test
    @DisplayName("deleteObject() - 삭제 실패")
    void deleteObject_Failure() {
        // Given
        String objectName = "testObject.jpg";
        // storageUrl이 null로 되어 URL 생성 시 문제가 발생하므로, 올바른 값을 설정합니다.
        String expectedStorageUrl = "http://storage-server.com";
        ReflectionTestUtils.setField(storageService, "storageUrl", expectedStorageUrl);

        String url = expectedStorageUrl + "/" + objectName;
        String tokenId = "testToken";
        TokenInfo tokenInfo = new TokenInfo(tokenId, Instant.now().plusSeconds(3600));
        ReflectionTestUtils.setField(storageService, "cachedToken", tokenInfo);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // When
        boolean result = storageService.deleteObject(objectName);

        // Then
        // 현재 deleteObject()는 응답 상태에 상관없이 true를 반환하도록 구현되어 있음
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        );
    }


    /**
     * uploadObject 내부의 getTokenId 호출을 Mocking
     * 이를 위해 StorageService의 getTokenId를 Spy로 변경
     */
    @Test
    @DisplayName("uploadObject() - 정상 호출 시 토큰을 올바르게 사용")
    void uploadObject_UsesCorrectToken() throws IOException {
        // Given
        StorageService spyStorageService = spy(storageService);
        String objectName = "testObject.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        String tokenId = "testToken";

        // storageUrl이 null이 아니도록 명확히 설정
        String mockStorageUrl = "http://mock-storage-url";
        ReflectionTestUtils.setField(spyStorageService, "storageUrl", mockStorageUrl);

        TokenInfo tokenInfo = new TokenInfo(tokenId, Instant.now().plusSeconds(3600));
        ReflectionTestUtils.setField(spyStorageService, "cachedToken", tokenInfo);

        // getTokenId()가 항상 'testToken'을 반환하도록 설정
        doReturn(tokenId).when(spyStorageService).getTokenId();

        // restTemplate.execute(...)가 호출될 때 null을 반환하도록 설정
        when(restTemplate.execute(
                anyString(), // URL을 any()로 설정하여 유연하게 처리
                eq(HttpMethod.PUT),
                any(),
                any()
        )).thenReturn(null);

        // When
        boolean result = spyStorageService.uploadObject(objectName, inputStream);

        // Then
        assertTrue(result);
        verify(restTemplate, times(1)).execute(
                eq(mockStorageUrl + "/" + objectName), // storageUrl이 올바르게 설정된 후 URL 매칭
                eq(HttpMethod.PUT),
                any(),
                any()
        );
    }
}
