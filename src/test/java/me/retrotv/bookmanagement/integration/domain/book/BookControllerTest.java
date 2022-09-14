package me.retrotv.bookmanagement.integration.domain.book;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.book.BookDTO;
import me.retrotv.bookmanagement.domain.image.ImageDTO;
import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.member.MemberService;
import me.retrotv.bookmanagement.domain.member.Member.Role;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookControllerTest {

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
    private String imageId;
    private String bookId;

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
        MockMultipartFile mockFile = new MockMultipartFile("files",
                                                           "XL.jpeg",
                                                           "image/jpeg",
                                                           fis
                                                          );

        return this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/save")
                                                          .file(mockFile)
                                                          .header(this.accessHeader, "Bearer " + this.accessToken)
                                                          .header(this.refreshHeader, "Bearer " + this.refreshToken));
    }

    private ResultActions uploadBookWithAuth(String imageId) throws Exception {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("프리렉")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(null)
                                        .name("이동욱")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        ImageDTO imageDTO = ImageDTO.builder()
                                    .id(Long.valueOf(imageId))
                                    .build();

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(null)
                                           .title("스프링 부트와 AWS로 혼자 구현하는 웹 서비스")
                                           .isbn("9788965402602")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .imageDTO(imageDTO)
                                           .build();
        String book = this.objectMapper.writeValueAsString(bookDTO);

        return this.mockMvc.perform(MockMvcRequestBuilders.post("/api/book/save")
                                                          .contentType(APPLICATION_JSON)
                                                          .content(book)
                                                          .header(this.accessHeader, "Bearer " + this.accessToken)
                                                          .header(this.refreshHeader, "Bearer " + this.refreshToken));
    }

    @BeforeAll
    public void joinAndGetToken() throws Exception {        
        this.memberService.saveMember(MemberDTO.builder().id(null).username("bookcontroller").password("bookcontroller").email("bookcontroller@test.com").role(Role.ADMIN).build());
        
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap("bookcontroller", "bookcontroller");
        MvcResult result = performPost("/api/member/login", APPLICATION_JSON, map).andDo(print())
                                                                                       .andExpectAll(status().isOk())
                                                                                       .andReturn();


        this.accessToken = result.getResponse().getHeader(accessHeader);
        this.refreshToken = result.getResponse().getHeader(refreshHeader);

        result = this.uploadImageWithAuth().andDo(print())
                                           .andExpect(status().isOk())
                                           .andExpect(jsonPath("$.success").value("true"))
                                           .andExpect(jsonPath("$.message").value("표지 저장에 성공했습니다."))
                                           .andReturn();
        BasicResult basicResult = this.objectMapper.readValue(result.getResponse().getContentAsString(Charset.forName("utf8")), BasicResult.class);
        this.imageId = basicResult.getData().toString().replace("{imageId=", "").replace("}", "");
    }

    @Test
    @Order(1)
    @DisplayName("책 저장")
    void saveBook() throws Exception {
        this.uploadBookWithAuth(this.imageId).andDo(print())
                                             .andExpect(status().isOk())
                                             .andExpect(jsonPath("$.success").value("true"))
                                             .andExpect(jsonPath("$.message").value("책 저장이 완료 되었습니다."))
                                             .andExpect(jsonPath("$.data").isEmpty())
                                             .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("책 조회")
    void searchBook() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/book/search")
                                                                      .param("title", "AWS")
                                                                      .param("authorName", "")
                                                                      .param("publisherName", "")
                                                                      .param("username", "bookcontroller")
                                                                      .param("size", "10")
                                                                      .param("page", "0"))
                                       .andDo(print())
                                       .andExpect(status().isOk())
                                       .andExpect(jsonPath("$.success").value("true"))
                                       .andExpect(jsonPath("$.message").isEmpty())
                                       .andExpect(jsonPath("$.data").isNotEmpty())
                                       .andExpect(jsonPath("$.count").value(1))
                                       .andReturn();

        BasicResult basicResult = this.objectMapper.readValue(result.getResponse().getContentAsString(Charset.forName("utf8")), BasicResult.class);
        JsonNode node = this.objectMapper.readTree(this.objectMapper.writeValueAsString(basicResult.getData()));
        this.bookId = node.get(0).get("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("책 수정")
    void updateBook() throws Exception {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("위키북스")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(null)
                                        .name("장정우")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        ImageDTO imageDTO = ImageDTO.builder()
                                    .id(Long.valueOf(this.imageId))
                                    .build();

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(Long.valueOf(this.bookId))
                                           .title("스프링 부트 핵심 가이드")
                                           .isbn("9791158393083")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .imageDTO(imageDTO)
                                           .build();

        String book = this.objectMapper.writeValueAsString(bookDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/book/update")
                                                   .contentType(APPLICATION_JSON)
                                                   .content(book)
                                                   .header(this.accessHeader, "Bearer " + this.accessToken)
                                                   .header(this.refreshHeader, "Bearer " + this.refreshToken))
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value("true"))
                    .andExpect(jsonPath("$.message").value("책 수정이 완료 되었습니다."))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @Order(4)
    @DisplayName("책 삭제")
    void deleteBook() throws Exception {
        BookDTO bookDTO = BookDTO.builder()
                                 .id(Long.valueOf(this.bookId))
                                 .build();
        String book = this.objectMapper.writeValueAsString(bookDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/delete")
                                                   .contentType(APPLICATION_JSON)
                                                   .content(book)
                                                   .header(this.accessHeader, "Bearer " + this.accessToken)
                                                   .header(this.refreshHeader, "Bearer " + this.refreshToken))
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value("true"))
                    .andExpect(jsonPath("$.message").value("책 삭제가 완료 되었습니다."))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.count").value(0));
    }
}
