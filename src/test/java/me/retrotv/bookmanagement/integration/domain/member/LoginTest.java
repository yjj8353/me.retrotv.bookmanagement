package me.retrotv.bookmanagement.integration.domain.member;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.member.MemberService;
import me.retrotv.bookmanagement.domain.member.Member.Role;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
class LoginTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    EntityManager em;

    ObjectMapper objectMapper = new ObjectMapper();

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static final String KEY_DATA = "data";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "123456789";
    private static final String LOGIN_URL = "/api/member/login";

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @BeforeEach
    @Transactional
    private void init() {
        memberService.saveMember(
            MemberDTO.builder()
                     .username(USERNAME)
                     .password(PASSWORD)
                     .realName("This is real name")
                     .nickName("This is nickname")
                     .role(Role.ADMIN)
                     .build()
        );
    }

    private Map<String, Map<String, String>> getUsernamePasswordMultiValueMap(String username, String password) {

        // *주의: password는 MemberService 단에서 암호화 하므로, 여기서 암호화 할 필요 없음
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);

        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        data.put(KEY_DATA, map);

        return data;
    }

    private ResultActions performPost(String url, MediaType mediaType, Map<String, Map<String, String>> usernamePasswordMap) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                     .contentType(mediaType)
                                                     .content(objectMapper.writeValueAsString(usernamePasswordMap)));
    }

    @Test
    @Transactional
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME, PASSWORD);

        MvcResult result = performPost(LOGIN_URL, APPLICATION_JSON, map).andDo(print())
                                                                        .andExpect(status().isOk())
                                                                        .andReturn();

        assertNotNull(result.getResponse().getHeader(accessHeader));
        assertNotNull(result.getResponse().getHeader(refreshHeader));
    }

    @Test
    @Transactional
    @DisplayName("로그인 실패 - 사유: 잘못된 아이디")
    void loginFailByWrongId() throws Exception {
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME + "123", PASSWORD);

        // 로그인에 실패하므로 Access Denied (403 Forbbiden) 에러가 발생함
        MvcResult result = performPost(LOGIN_URL, APPLICATION_JSON, map).andDo(print())
                                                                        .andExpect(status().isUnauthorized())
                                                                        .andReturn();

        // 로그인에 실패 했으므로, 토큰이 발급되지 않아 null이 뜨는게 정상임
        assertNull(result.getResponse().getHeader(accessHeader));
        assertNull(result.getResponse().getHeader(refreshHeader));
    }

    @Test
    @Transactional
    @DisplayName("로그인 실패 - 사유: 잘못된 패스워드")
    void loginFailByWrongPassword() throws Exception {
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME, PASSWORD + "123");

        MvcResult result = performPost(LOGIN_URL, APPLICATION_JSON, map).andDo(print())
                                                                        .andExpect(status().isUnauthorized())
                                                                        .andReturn();

        assertNull(result.getResponse().getHeader(accessHeader));
        assertNull(result.getResponse().getHeader(refreshHeader));
    }
}