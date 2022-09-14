package me.retrotv.bookmanagement.integration.domain.jwt;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import me.retrotv.bookmanagement.domain.jwt.JwtService;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.member.Member.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class JwtServiceTest {
    
    @Autowired
    JwtService jwtService;
    
    @Autowired
    MemberRepository memberRepository;
    
    @Autowired
    EntityManager em;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.header}")
    private String accessHeader;
    
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";

    private String username = "username";

    @BeforeEach
    void init() {
        Member member = Member.builder()
                              .username(username)
                              .password(delegatingPasswordEncoder.encode("1234567890"))
                              .realName("This is real name")
                              .nickName("This is nickname")
                              .role(Role.ADMIN)
                              .build();
        memberRepository.save(member);
        clear();
    }

    private void clear() { 
        em.flush();
        em.clear();
    }

    private DecodedJWT getVerify(String token) {
        return JWT.require(Algorithm.HMAC512(secret)).build().verify(token.replace(BEARER, ""));
    }

    private HttpServletRequest setRequest(String accessToken, String refreshToken) throws IOException {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse, accessToken, refreshToken);

        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        httpServletRequest.addHeader(accessHeader, BEARER+headerAccessToken);
        httpServletRequest.addHeader(refreshHeader, BEARER+headerRefreshToken);

        return httpServletRequest;
    }

    @Test
    @DisplayName("AccessToken 발급")
    void createAccessToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        DecodedJWT verify = getVerify(accessToken);

        String subject = verify.getSubject();
        String findUsername = verify.getClaim(USERNAME_CLAIM).asString();

        assertEquals(findUsername, username);
        assertEquals(ACCESS_TOKEN_SUBJECT, subject);
    }

    @Test
    @DisplayName("RefreshToken 발급")
    void createRefreshToken() throws Exception {
        String refreshToken = jwtService.createRefreshToken();
        DecodedJWT verify = getVerify(refreshToken);
        
        String subject = verify.getSubject();
        String username = verify.getClaim(USERNAME_CLAIM).asString();

        assertEquals(REFRESH_TOKEN_SUBJECT, subject);
        assertNull(username);
    }

    @Test
    @DisplayName("RefreshToken 업데이트")
    void updateRefreshToken() throws InterruptedException {
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();

        /*
         * 추측 상 Refresh Token 발행 시, 사용하는 시드 값 중에 현재 시각 값이 있는것으로 추정됨
         * sleep을 하지 않으면 refreshToken과 reIssuedRefreshToken 값이 동일하게 발행 되므로
         * 이 코드는 sonarlint warning이 뜨더라도 무시할 것.
         */
        Thread.sleep(1000);
        
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, reIssuedRefreshToken);
        clear();
        
        // 최초로 발급받은 Refresh Token은 더이상 존재하지 않으므로 조회되지 않음
        assertTrue(memberRepository.findByRefreshToken(refreshToken).isEmpty());

        // 처음 발급받은 Refresh Token과 새로 발급받은 Refresh Token은 서로 다른 토큰이어야 함
        assertNotEquals(refreshToken, reIssuedRefreshToken);
        assertEquals(memberRepository.findByRefreshToken(reIssuedRefreshToken).get().getUsername(), username);
    }

    @Test
    @DisplayName("RefreshToken username으로 제거")
    void destroyRefreshTokenByUsername() {
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();

        jwtService.destroyRefreshToken(username, null);
        clear();

        assertTrue(memberRepository.findByRefreshToken(refreshToken).isEmpty());

        Member member = memberRepository.findByUsername(username).get();
        assertNull(member.getRefreshToken());
    }

    @Test
    @DisplayName("RefreshToken refreshToken으로 제거")
    void destroyRefreshTokenByRefreshToken() {
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();

        jwtService.destroyRefreshToken(null, refreshToken);
        clear();

        assertTrue(memberRepository.findByRefreshToken(refreshToken).isEmpty());

        Member member = memberRepository.findByUsername(username).get();
        assertNull(member.getRefreshToken());
    }

    @Test
    @DisplayName("Token 유효성 체크")
    void checkValidToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        assertTrue(jwtService.isTokenValid(accessToken));
        assertTrue(jwtService.isTokenValid(refreshToken));
    }

    @Test
    @DisplayName("AccessToken 헤더 설정")
    void setAccessTokenHeader() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setAccessTokenHeader(mockHttpServletResponse, accessToken);
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);

        assertEquals(headerAccessToken, accessToken);
    }

    @Test
    @DisplayName("RefreshToken 헤더 설정")
    void setRefreshTokenHeader() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setRefreshTokenHeader(mockHttpServletResponse, refreshToken);
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        assertEquals(headerRefreshToken, refreshToken);
    }

    @Test
    @DisplayName("Token 전송")
    void sendAccessAndRefreshToken() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        assertEquals(headerAccessToken, accessToken);
        assertEquals(headerRefreshToken, refreshToken);
    }

    @Test
    @DisplayName("AccessToken 추출")
    void extractAccessToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String extractAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));

        assertEquals(extractAccessToken, accessToken);
        assertEquals(getVerify(extractAccessToken).getClaim(USERNAME_CLAIM).asString(), username);
    }

    @Test
    @DisplayName("RefreshToken 추출")
    void extractRefreshToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String extractRefreshToken = jwtService.extractRefreshToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));

        assertEquals(extractRefreshToken, refreshToken);
        assertEquals(REFRESH_TOKEN_SUBJECT, getVerify(extractRefreshToken).getSubject());
    }

    @Test
    @DisplayName("Username 추출")
    void extractUsername() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String requestAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));
        String extractUsername = jwtService.extractUsername(requestAccessToken).orElseThrow(() -> new Exception("토큰이 없습니다."));

        assertEquals(extractUsername, username);
    }
}
