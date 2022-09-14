package me.retrotv.bookmanagement.unit.domain.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import me.retrotv.bookmanagement.config.security.SpringSecurityConfig;
import me.retrotv.bookmanagement.domain.image.ImageController;
import me.retrotv.bookmanagement.domain.image.ImageService;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ImageController.class,
            excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SpringSecurityConfig.class)
            })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Image Controller 계층 테스트")
class ImageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    private MockMultipartFile getMockMultiFile() throws IOException {
        String path = "src/test/java/me/retrotv/bookmanagement/unit/domain/image/XL.jpeg";
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile mockFile = new MockMultipartFile("files", "XL.jpeg", "image/jpeg", fis);
    
        return mockFile;
    }

    @Nested
    @DisplayName("이미지 업로드")
    class imageUpload {

        @Test
        @DisplayName("성공")
        void success() throws Exception {

            /* give */
            MockMultipartFile mockFile = getMockMultiFile();
            Mockito.when(imageService.saveImage(mockFile)).thenReturn(new BasicResult("표지 저장에 성공했습니다."));

            /* when */
            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/save")
                                                  .file(mockFile))
                   .andDo(print())

                   /* then */
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value("true"))
                   .andExpect(jsonPath("$.message").value("표지 저장에 성공했습니다."))
                   .andExpect(jsonPath("$.data").isEmpty())
                   .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("실패 - 사유: 프론트엔드에서 넘어온 MultipartFile 객체가 없음")
        void failMultipartFileIsNull() throws Exception {
            try {

                /* when */
                mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/save")
                                                      .file(null));
            } catch(IllegalArgumentException e) {

                /* then */
                assertEquals(e.getClass(), IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("이미지 다운로드")
    class imageDownload {

        @Test
        @DisplayName("성공")
        void success() throws Exception {

            /* give */
            byte[] data = "1234567890".getBytes();
            Map<String, Object> map = new HashMap<>();
            map.put("imageData", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(data));

            Mockito.when(imageService.downloadImage(1L)).thenReturn(new BasicResult(null, map));

            /* when */
            mockMvc.perform(MockMvcRequestBuilders.get("/api/image/download")
                                                  .param("imageId", "1"))
                   .andDo(print())

                   /* then */
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value("true"))
                   .andExpect(jsonPath("$.message").isEmpty())
                   .andExpect(jsonPath("$.data").isNotEmpty())
                   .andExpect(jsonPath("$.count").value(1));
        }

        @Test
        @DisplayName("성공 - ID 값이 null")
        void successIdIsNull() throws Exception {

            /*
             * 나중에 noimage.jpeg 파일을 byte화 시켜서 비교할 것. 
             */

            /* give */
            byte[] data = "1234567890".getBytes();
            Map<String, Object> map = new HashMap<>();
            map.put("imageData", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(data));

            Mockito.when(imageService.downloadImage(1L)).thenReturn(new BasicError("ID 값이 null 입니다.", map, HttpStatus.NOT_FOUND));

            /* when */
            mockMvc.perform(MockMvcRequestBuilders.get("/api/image/download")
                                                  .param("imageId", "1"))
                   .andDo(print())

                   /* then */
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.success").value("false"))
                   .andExpect(jsonPath("$.message").value("ID 값이 null 입니다."))
                   .andExpect(jsonPath("$.data").isNotEmpty())
                   .andExpect(jsonPath("$.count").value(1));
        }

        @Test
        @DisplayName("실패 - 사유: 없는 ID 값으로 조회를 시도")
        void failNoSuchElement() throws Exception {

            /* give */
            byte[] data = "1234567890".getBytes();
            Map<String, Object> map = new HashMap<>();
            map.put("imageData", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(data));

            Mockito.when(imageService.downloadImage(1L)).thenReturn(new BasicError("해당 ID 값으로 조회된 이미지가 없습니다.", map, HttpStatus.NOT_FOUND));

            /* when */
            mockMvc.perform(MockMvcRequestBuilders.get("/api/image/download")
                                                  .param("imageId", "1"))
                   .andDo(print())

                   /* then */
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.success").value("false"))
                   .andExpect(jsonPath("$.message").value("해당 ID 값으로 조회된 이미지가 없습니다."))
                   .andExpect(jsonPath("$.data").isNotEmpty())
                   .andExpect(jsonPath("$.count").value(1));
        }
    }
}
