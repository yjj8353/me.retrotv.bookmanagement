package me.retrotv.bookmanagement.integration.domain.image;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.member.MemberService;
import me.retrotv.bookmanagement.domain.member.Member.Role;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageControllerTest {

    /*
     * @TestInstance(TestInstance.Lifecycle.PER_CLASS) 어노테이션은 테스트 라이프 사이클을 클래스 단위로 설정할 수 있도록 해준다.
     * 테스트 라이프 사이클을 클래스 단위로 설정하면, @BeforeAll, @AfterAll 어노테이션이 달린 함수를 static으로 설정하지 않아도 되며,
     * 여러개의 테스트 케이스끼리 전역변수가 공유되도록 할 수 있다.
     */

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private String accessToken;
    private String refreshToken;

    private ResultActions performPost(String url, MediaType mediaType, Map<String, Map<String, String>> usernamePasswordMap) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                     .contentType(mediaType)
                                                     .content(objectMapper.writeValueAsString(usernamePasswordMap)));
    }

    private Map<String, Map<String, String>> getUsernamePasswordMultiValueMap(String username, String password) {

        // *주의: password는 MemberService 단에서 암호화 하므로, 여기서 암호화 할 필요 없음
        Map<String, String> map = new LinkedHashMap<>();
        map.put("username", username);
        map.put("password", password);

        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        data.put("data", map);

        return data;
    }

    private ResultActions uploadImageWithAuth() throws Exception {
        String path = "src/test/java/me/retrotv/bookmanagement/integration/domain/image/XL.jpeg";
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile mockFile = new MockMultipartFile("files", "XL.jpeg", "image/jpeg", fis);

        return this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/save")
                                                          .file(mockFile)
                                                          .header(this.accessHeader, "Bearer " + this.accessToken)
                                                          .header(this.refreshHeader, "Bearer " + this.refreshToken));
    }

    @BeforeAll
    public void joinAndGetToken() throws Exception {
        this.memberService.saveMember(MemberDTO.builder().id(null).username("imagecontroller").password("imagecontroller").email("imagecontrller@test.com").role(Role.ADMIN).build());
                                                   
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap("imagecontroller", "imagecontroller");

        MvcResult result = performPost("/api/member/login", APPLICATION_JSON, map).andDo(print())
                                                                                       .andExpectAll(status().isOk())
                                                                                       .andReturn();

        this.accessToken = result.getResponse().getHeader(accessHeader);
        this.refreshToken = result.getResponse().getHeader(refreshHeader);
    }

    @Nested
    @DisplayName("이미지 업로드")
    class ImageUpload {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            uploadImageWithAuth().andDo(print())
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.success").value("true"))
                                 .andExpect(jsonPath("$.message").value("표지 저장에 성공했습니다."));
        }
    }

    @Nested
    @DisplayName("이미지 다운로드")
    class ImageDownload {
        
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            MvcResult result = uploadImageWithAuth().andReturn();
            BasicResult basicResult = objectMapper.readValue(result.getResponse().getContentAsString(Charset.forName("utf8")), BasicResult.class);
            String imageId = basicResult.getData().toString().replace("{imageId=", "").replace("}", "");

            mockMvc.perform(MockMvcRequestBuilders.get("/api/image/download")
                                                    .param("imageId", imageId))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value("true"))
                   .andExpect(jsonPath("$.message").isEmpty())
                   .andExpect(jsonPath("$.data").isNotEmpty())
                   .andExpect(jsonPath("$.count").value(1));
        }

        @Test
        @DisplayName("성공 - imageId가 null일 경우 noimage.jpeg를 반환함")
        void successNoImage() throws Exception {
            String imageId = null;
            mockMvc.perform(MockMvcRequestBuilders.get("/api/image/download")
                                                  .param("imageId", imageId))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.success").value("true"))
                   .andExpect(jsonPath("$.message").isEmpty())
                   .andExpect(jsonPath("$.data").isNotEmpty())
                   .andExpect(jsonPath("$.count").value(1));
        }
    }
}
