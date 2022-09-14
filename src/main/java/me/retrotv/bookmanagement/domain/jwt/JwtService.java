package me.retrotv.bookmanagement.domain.jwt;

import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;

/**
 * JWT Token 관련 기능을 집약한 객체
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    private final MemberRepository memberRepository;

    private static final String NO_USER = "회원이 없습니다";

    /*
     * JWT Token 암복호화시 사용할 키 값
     */
    @Value("${jwt.secret}")
    private String secret;

    /*
     * JWT Access Token 유효시간 (초)
     */
    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    /*
     * JWT Refresh Token 유효시간 (초)
     */
    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;

    /*
     * HTTP Header에 Access Token을 포함시킬 때, Access Token임을 식별하기 위한 키 값
     */
    @Value("${jwt.access.header}")
    private String accessHeader;

    /*
     * HTTP Header에 Refresh Token을 포함시킬 때, Refresh Token임을 식별하기 위한 키 값
     */
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";

    /**
     * Access Token 값을 생성하고 반환하는 함수.
     * @param username 로그인한 유저의 ID
     * @return JWT Access Token 값
     */
    public String createAccessToken(String username) {
        log.debug("새로운 Access Token 생성");
        return JWT.create()
                  .withSubject(ACCESS_TOKEN_SUBJECT)
                  .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000))
                  .withClaim(USERNAME_CLAIM, username)
                  .sign(Algorithm.HMAC512(secret));
    }
    
    /**
     * Refresh Token 값을 생성하고 반환하는 함수.
     * @return JWT Refresh Token 값
     */
    public String createRefreshToken() {
        log.debug("새로운 Refresh Token 생성");
        return JWT.create()
                  .withSubject(REFRESH_TOKEN_SUBJECT)
                  .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
                  .sign(Algorithm.HMAC512(secret));
    }

    /**
     * 로그인한 유저의 Refresh Token이 갱신되었을 경우, 로그인한 유저({@link Member}) 엔티티의 Refresh Token을 업데이트 하는 함수.
     * @param name Refresh Token을 업데이트 할 유저의 ID
     * @param refreshToken 새로 발급된 Refresh Token
     */
    @Transactional
    public void updateRefreshToken(String username, String refreshToken) {
        memberRepository.findByUsername(username)
                        .ifPresentOrElse(
                            member -> member.updateRefreshToken(refreshToken),
                            () -> new UsernameNotFoundException(NO_USER)
                        );
    }

    /**
     * 로그인한 유저({@link Member}) 엔티티의 Refresh Token을 파기하는 함수.
     * <p>로그인한 유저의 username 및 refresh token 값을 이용해 파기한다.</p>
     * @param name Refresh Token을 파기할 유저의 ID
     * @param refreshToken 파기될 Refresh Token 값
     */
    @Transactional
    public void destroyRefreshToken(String name, String refreshToken) {
        if(name != null && !name.isEmpty()) {
            memberRepository.findByUsername(name)
                            .ifPresentOrElse(
                                Member::destroyRefreshToken,
                                () -> new UsernameNotFoundException(NO_USER)
                            );
        }

        if(refreshToken != null && !refreshToken.isEmpty()) {
            memberRepository.findByRefreshToken(refreshToken)
                            .ifPresentOrElse(
                                Member::destroyRefreshToken,
                                () -> new UsernameNotFoundException(NO_USER)
                            );
        }
    }
    
    /**
     * HTTP 요청 헤더에 포함된 Access Token을 {@link Optional} 객체로 감싸 반환하는 함수.
     * @param request HTTP 요청 객체
     * @return Access Token 값이 포함된 {@link Optional} 객체
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                       .filter(accessToken -> accessToken.startsWith(BEARER))
                       .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    /**
     * HTTP 요청 헤더에 포함된 Refresh Token을 {@link Optional} 객체로 감싸 반환하는 함수.
     * @param request HTTP 요청 객체
     * @return Refresh Token 값이 포함된 {@link Optional} 객체
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                       .filter(refreshToken -> refreshToken.startsWith(BEARER))
                       .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * Access Token 값에 포함된 유저 ID 값을 {@link Optional} 객체로 감싸 반환하는 함수.
     * @param accessToken Access Token 값
     * @return 유저 ID가 포함된 {@link Optional} 객체
     */
    public Optional<String> extractUsername(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getClaim(USERNAME_CLAIM).asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * JWT Token이 유효한지 확인하고 유효여부를 반환하는 함수.
     * @param token Access Token 혹은 Refresh Token 값
     * @return 해당 토큰의 유효여부
     */
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 클라이언트에게 Access Token과 Refresh Token 값을 전달하기 위해 헤더에 Token 값을 세팅하는 함수.
     * @param response HTTP 응답 객체
     * @param accessToken Acess Token 값
     * @param refreshToken Refresh Token 값
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }

    /**
     * 클라이언트에게 Access Token 값을 전달하기 위해 헤더에 Access Token 값을 세팅하는 함수.
     * 이 함수는 Refresh Token만 유효할 경우 클라이언트에 Access Token을 전송함과 동시에 기존의 Access Token이
     * 파기되었음을 알리기 위해 Expectation Failed Error Status도 응답헤더에 세팅한다.
     * @param response HTTP 응답 객체
     * @param accessToken Access Token 값
     */
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
        setAccessTokenHeader(response, accessToken);
    }

    /**
     * HTTP 응답 객체 헤더에 Access Token 값을 세팅하는 함수.
     * @param response HTTP 응답 객체
     * @param accessToken Access Token 값
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    /**
     * HTTP 응답 객체 헤더에 Refresh Token 값을 세팅하는 함수.
     * @param response HTTP 응답 객체
     * @param refreshToken Refresh Token 값
     */
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /**
     * refreshToken 값을 이용해 Member의 username을 찾는 함수.
     * @param refreshToken 검색조건으로 사용 할 Refresh Token 문자열
     * @return username 문자열, 해당 유저가 없으면 null
     */
    public String findMemberByRefreshToken(String refreshToken) {
        Optional<Member> member = memberRepository.findByRefreshToken(refreshToken);
        if(member.isPresent()) {
            return member.get().getUsername();
        }

        return null; 
    }
}
