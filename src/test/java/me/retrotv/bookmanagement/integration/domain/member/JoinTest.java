package me.retrotv.bookmanagement.integration.domain.member;

import java.util.LinkedHashMap;
import java.util.Map;

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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JoinTest {
    
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

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
    @DisplayName("첫번째 관리자 자동 인증 활성화")
    void autoCertifyFirstAdmin() throws Exception {
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME, PASSWORD);

        MvcResult result = performPost(LOGIN_URL, APPLICATION_JSON, map).andDo(print())
                                                                        .andExpect(status().isOk())
                                                                        .andReturn();

        assertNotNull(result.getResponse().getHeader(accessHeader));
        assertNotNull(result.getResponse().getHeader(refreshHeader));
    }

    @Test
    @Transactional
    @DisplayName("두번째 관리자 자동 인증 비활성화")
    void notAutoCertifySecondAdmin() throws Exception {
        memberService.saveMember(
            MemberDTO.builder()
                     .username(USERNAME + "2")
                     .password(PASSWORD)
                     .realName("This is real name")
                     .nickName("This is nickname")
                     .role(Role.ADMIN)
                     .build()
        );

        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME + "2", PASSWORD);

        MvcResult result = performPost(LOGIN_URL, APPLICATION_JSON, map).andDo(print())
                                                                        .andExpect(status().isUnauthorized())
                                                                        .andReturn();

        assertNull(result.getResponse().getHeader(accessHeader));
        assertNull(result.getResponse().getHeader(refreshHeader));
    }
}
