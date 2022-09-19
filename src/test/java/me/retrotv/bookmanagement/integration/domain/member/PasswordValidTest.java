package me.retrotv.bookmanagement.integration.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.member.MemberDTO;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordValidTest {

    @Nested
    @DisplayName("패스워드 유효성 검사")
    class PasswordValidCheck {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        final String url = "/api/member/join";
        final String username = "username";
        final String email = "test@test.com";

        @ParameterizedTest(name = "[{index}] password: {0}")
        @ValueSource(strings = { "", "1D3%sd&", "1D3%sd&1D3%sd&1D3%sd&1D3%sd&", "4040829237348123", "asdmlwqeknqsddkw" })
        @DisplayName("실패")
        void fail(String password) throws Exception {
            MemberDTO memberDTO = MemberDTO.builder().username(username).password(password).email(email).build();
            String member = this.objectMapper.writeValueAsString(memberDTO);
            this.mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                       .contentType(APPLICATION_JSON)
                                                       .content(member))
                                                       .andDo(print())
                                                       .andExpect(status().isBadRequest())
                                                       .andReturn();
        }

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            final String password = "!Q@W3e4r%T6y";
            MemberDTO memberDTO = MemberDTO.builder().username(username).password(password).email(email).build();
            String member = this.objectMapper.writeValueAsString(memberDTO);
            this.mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                       .contentType(APPLICATION_JSON)
                                                       .content(member))
                                                       .andDo(print())
                                                       .andExpect(status().isOk())
                                                       .andReturn();
        }
    }
}
