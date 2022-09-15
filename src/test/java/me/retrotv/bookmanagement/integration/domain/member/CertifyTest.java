package me.retrotv.bookmanagement.integration.domain.member;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.member.MemberService;
import me.retrotv.bookmanagement.domain.member.Member.Role;
import me.retrotv.bookmanagement.util.MailUtil;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CertifyTest {
    
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    ObjectMapper objectMapper = new ObjectMapper();
    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static final String USERNAME = "username";
    private static final String PASSWORD = "123456789";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DATA = "data";
    private String passcode = null;

    private Map<String, Map<String, String>> getUsernamePasswordMultiValueMap(String username, String password) {

        // *주의: password는 MemberService 단에서 암호화 하므로, 여기서 암호화 할 필요 없음
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);

        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        data.put(KEY_DATA, map);

        return data;
    }

    @Test
    @Order(1)
    @DisplayName("가입 신청")
    void joinMember() {
        memberService.saveMember(
            MemberDTO.builder()
                     .username(USERNAME)
                     .password(PASSWORD)
                     .realName("This is real name")
                     .nickName("This is nickname")
                     .email("test@test.com")
                     .role(Role.ADMIN)
                     .build()
        );

        Optional<Member> member = memberRepository.findByUsername(USERNAME);

        if(member.isPresent()) {
            this.passcode = member.get().getPasscode();
            log.info("passcode 값: " + this.passcode);
            assertNotNull(member.get().getPasscode());
        }
    }

    @Test
    @Order(2)
    @DisplayName("이메일 전송 테스트")
    void sendMail() {
        String address = "test@test.com";
        String subject = "가입 확인용 이메일";

        log.debug("링크: " + String.format("<a>https://localhost:8080/certify?username=%s&passcode=%s</a>", USERNAME, this.passcode));

        String text = "<p>다음의 링크를 클릭하면 가입이 완료됩니다.</p>"
                    + String.format("<a href='https://localhost:8080/certify?username=%s&passcode=%s'>https://localhost:8080/certify?username=%s&passcode=%s</a>", USERNAME, this.passcode, USERNAME, this.passcode);
        MailUtil.sendMail(address, subject, text);
    }

    @Test
    @Order(3)
    @DisplayName("가입 인증")
    void certifyMember() throws Exception {
        Map<String, Map<String, String>> map = getUsernamePasswordMultiValueMap(USERNAME, PASSWORD);

        this.mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/member/certify?username=%s&passcode=%s", USERNAME, this.passcode))
                                                              .contentType(APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(map)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("This is real name님, 정상적으로 인증 되었습니다."));
    }

    @Test
    @Order(4)
    @DisplayName("가입 확인")
    void checkCertify() {
        memberRepository.findByUsername(USERNAME).ifPresent(member -> 
            assertTrue(member.isCertified())
        );
    }
}
