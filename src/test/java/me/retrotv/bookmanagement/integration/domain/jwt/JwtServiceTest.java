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
    @DisplayName("AccessToken ??????")
    void createAccessToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        DecodedJWT verify = getVerify(accessToken);

        String subject = verify.getSubject();
        String findUsername = verify.getClaim(USERNAME_CLAIM).asString();

        assertEquals(findUsername, username);
        assertEquals(ACCESS_TOKEN_SUBJECT, subject);
    }

    @Test
    @DisplayName("RefreshToken ??????")
    void createRefreshToken() throws Exception {
        String refreshToken = jwtService.createRefreshToken();
        DecodedJWT verify = getVerify(refreshToken);
        
        String subject = verify.getSubject();
        String username = verify.getClaim(USERNAME_CLAIM).asString();

        assertEquals(REFRESH_TOKEN_SUBJECT, subject);
        assertNull(username);
    }

    @Test
    @DisplayName("RefreshToken ????????????")
    void updateRefreshToken() throws InterruptedException {
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, refreshToken);
        clear();

        /*
         * ?????? ??? Refresh Token ?????? ???, ???????????? ?????? ??? ?????? ?????? ?????? ?????? ??????????????? ?????????
         * sleep??? ?????? ????????? refreshToken??? reIssuedRefreshToken ?????? ???????????? ?????? ?????????
         * ??? ????????? sonarlint warning??? ???????????? ????????? ???.
         */
        Thread.sleep(1000);
        
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(username, reIssuedRefreshToken);
        clear();
        
        // ????????? ???????????? Refresh Token??? ????????? ???????????? ???????????? ???????????? ??????
        assertTrue(memberRepository.findByRefreshToken(refreshToken).isEmpty());

        // ?????? ???????????? Refresh Token??? ?????? ???????????? Refresh Token??? ?????? ?????? ??????????????? ???
        assertNotEquals(refreshToken, reIssuedRefreshToken);
        assertEquals(memberRepository.findByRefreshToken(reIssuedRefreshToken).get().getUsername(), username);
    }

    @Test
    @DisplayName("RefreshToken username?????? ??????")
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
    @DisplayName("RefreshToken refreshToken?????? ??????")
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
    @DisplayName("Token ????????? ??????")
    void checkValidToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        assertTrue(jwtService.isTokenValid(accessToken));
        assertTrue(jwtService.isTokenValid(refreshToken));
    }

    @Test
    @DisplayName("AccessToken ?????? ??????")
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
    @DisplayName("RefreshToken ?????? ??????")
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
    @DisplayName("Token ??????")
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
    @DisplayName("AccessToken ??????")
    void extractAccessToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String extractAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("????????? ????????????."));

        assertEquals(extractAccessToken, accessToken);
        assertEquals(getVerify(extractAccessToken).getClaim(USERNAME_CLAIM).asString(), username);
    }

    @Test
    @DisplayName("RefreshToken ??????")
    void extractRefreshToken() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String extractRefreshToken = jwtService.extractRefreshToken(httpServletRequest).orElseThrow(() -> new Exception("????????? ????????????."));

        assertEquals(extractRefreshToken, refreshToken);
        assertEquals(REFRESH_TOKEN_SUBJECT, getVerify(extractRefreshToken).getSubject());
    }

    @Test
    @DisplayName("Username ??????")
    void extractUsername() throws Exception {
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String requestAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("????????? ????????????."));
        String extractUsername = jwtService.extractUsername(requestAccessToken).orElseThrow(() -> new Exception("????????? ????????????."));

        assertEquals(extractUsername, username);
    }
}
