package me.retrotv.bookmanagement.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.exception.NotCertificatedException;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 사용자 로그인을 처리하기 위해 {@link AbstractAuthenticationProcessingFilter}를 상속받은 필터 객체.
 * @version 1.0
 * @author yjj8353
 */
public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/api/member/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD); // => /login 의 요청에, POST로 온 요청에 매칭된다.

    private ObjectMapper objectMapper;
    private MemberRepository memberRepository;
    
    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, MemberRepository memberRepository) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.objectMapper = objectMapper;
        this.memberRepository = memberRepository;
    }
    
    /**
     * <p>* AbstractAuthenticationProcessingFilter 인터페이스 로부터 상속받음.</p>
     * <p>유저의 ID, PASSWORD를 이용해 사용자를 인증하고 인증된 경우 인증된 사용자 객체를 반환하거나 인증이 불완전할 경우 null을 반환하는 함수.</p>
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 인증시 인증된 사용자 객체, 인증되지 않았을 경우 null
     * @exception AuthenticationException
     * @exception IOException
     * @exception ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(messageBody);
        
        String username = node.get("data").get(USERNAME_KEY).asText();
        String password = node.get("data").get(PASSWORD_KEY).asText();

        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("회원이 없습니다."));
        if(!member.isCertified()) {
            throw new NotCertificatedException("인증되지 않은 사용자 입니다.\n다른 관리자의 인증이 필요합니다.");
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
