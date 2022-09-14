package me.retrotv.bookmanagement.integration.domain.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.image.ImageService;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImageServiceTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageService imageService;

    @Nested
    @DisplayName("이미지 업로드")
    class ImageSave {

        @Test
        @DisplayName("성공")
        void imageUpload() throws IOException {
            String path = "src/test/java/me/retrotv/bookmanagement/integration/domain/image/XL.jpeg";
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            MockMultipartFile mockFile = new MockMultipartFile("files",
                                                            "XL.jpeg",
                                                            "image/jpeg",
                                                            fis
                                                            );

            BasicResult result = imageService.saveImage(mockFile);

            assertEquals("표지 저장에 성공했습니다.", result.getMessage());
            assertEquals(HttpStatus.OK, result.getStatus());
        }
    }

    @Nested
    @DisplayName("이미지 다운로드")
    class ImageDownload {

        @Test
        @DisplayName("성공")
        void imageDownload() throws IOException {
            String path = "src/test/java/me/retrotv/bookmanagement/integration/domain/image/XL.jpeg";
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            MockMultipartFile mockFile = new MockMultipartFile("files",
                                                            "XL.jpeg",
                                                            "image/jpeg",
                                                            fis
                                                            );

            BasicResult result = imageService.saveImage(mockFile);
            Map<String, String> map = objectMapper.convertValue(result.getData(), new TypeReference<Map<String, String>>() {});
                        result = imageService.downloadImage(Long.valueOf(map.get("imageId")));

            assertNull(result.getMessage());
            assertNotEquals("", objectMapper.convertValue(result.getData(), new TypeReference<Map<String, String>>() {}).get("imageData"));
            assertEquals(1, result.getCount());
            assertEquals(HttpStatus.OK, result.getStatus());
        }

        @Test
        @DisplayName("성공 - imageId가 null일 경우 기본 noimage.jpeg를 다운받음")
        void imageDownloadFailIllegalArgumentException() {
            BasicResult result = imageService.downloadImage(null);

            assertEquals(null, result.getMessage());
            assertNotNull(result.getData());
            assertEquals(HttpStatus.OK, result.getStatus());
        }

        @Test
        @DisplayName("실패 - 사유: imageId로 조회된 이미지가 없음")
        void imageDownloadFailNoSuchElementException() {
            try {
                imageService.downloadImage(0L);
            } catch(NoSuchElementException e) {
                assertEquals("해당 ID 값으로 조회된 이미지가 없습니다.", e.getMessage());
            }
        }
    }
}
