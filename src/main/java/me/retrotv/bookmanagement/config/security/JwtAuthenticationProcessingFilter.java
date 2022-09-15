package me.retrotv.bookmanagement.config.security;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.domain.jwt.JwtService;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 인증을 위해 {@link OncePerRequestFilter}를 상속받아 처리하는 필터 객체.
 * @version 1.0
 * @author yjj8353
 */
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private static final List<String> NO_CHECK_URL_LIST = Arrays.asList("/", "/index.html", "/favicon.ico", "/member/login", "/member/join", "/member/logout", "/member/certify", "/member/password-change-none-auth", "/member/password-change-email-send", "/book/search", "/image/download", "/jwt/valid");

    /**
     * * OncePerRequestFilter 클래스 로부터 상속받음.
     * <p>HTTP 요청 객체에 포함된 Access Token 값과 Refresh Token 값을 추출한 뒤, 해당 Token 값이 유효한지 검사하고 조건에 따라 필터를 진행하거나 진행하지 않는다.</p>
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain Filter 체이닝을 위한 객체
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        /*
         * (1) 필터를 진행시킬 필요가 없는 요청은 곧바로 무시하고 필터에서 빠져나온다.
         * (2) Access Token과 Refresh Token이 유효한지 확인한다. 유효하면 Token 값이 세팅되고 유효하지 않으면 null이 세팅된다.
         * (3) 둘 다 유효하지 않은 경우, 401 Error Status (Unauthorized) 응답 상태를 반환하고 다시 로그인 하도록 유도한다.
         * (4) Refresh Token만 유효한 경우, 417 Error Status (Expectation Failed) 응답 상태와 새로 발급된 Access Token을 HTTP 응답 객체에 세팅하고 필터에서 빠져나온다.
         * (5) 그 외의 경우에는 인증된 사용자 정보를 SecurityContextHolder에 세팅하고 필터를 진행한다. 만약 Token이 둘 다 유효하지 않은 경우에는 401 Unauthorized 에러가 발생한다.
         */

        // (1)
        for(String url: NO_CHECK_URL_LIST) {
            if(request.getRequestURI().replace("/api", "").equals(url) || request.getRequestURI().contains("/icons") || request.getRequestURI().contains("/assets")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // (2)
        String accessToken = jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).orElse(null);
        String refreshToken = jwtService.extractRefreshToken(request).filter(jwtService::isTokenValid).orElse(null);

        // (3)
        if(accessToken == null && refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("isTokenValid", "false");
            return;
        }
        
        // (4)
        if(accessToken == null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }

        
        // (5)
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    /**
     * Access Token을 발급받은 사용자({@link Member})를 확인하고, 인증된 사용자 정보를 SecurityContextHolder에 세팅하는 함수.
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain Filter 체이닝을 위한 객체
     * @throws ServletException
     * @throws IOException
     */
    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent(
            accessToken -> jwtService.extractUsername(accessToken).ifPresent(
                username -> memberRepository.findByUsername(username).ifPresent(
                    this::saveAuthentication
                )
            )
        );

        // Token 인증이 완료 된 후, Access/Refresh Token 모두 갱신 후 헤더에 세팅.
        jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent(
            accessToken -> jwtService.extractUsername(accessToken).ifPresent(
                username -> {
                    String newAccessToken = jwtService.createAccessToken(username);
                    String newRefreshToken = jwtService.createRefreshToken();
                    jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);
                    jwtService.updateRefreshToken(username, newRefreshToken);
                }
            )
        );
        
        filterChain.doFilter(request, response);
    }

    /**
     * 인증된 사용자 정보 토큰을 발급받고, 해당 정보를 SecurityContextHolder에 세팅하는 함수.
     * @param member 사용자 정보가 담긴 엔티티 객체
     */
    private void saveAuthentication(Member member) {
        UserDetails userDetail = User.builder()
                                     .username(member.getUsername())
                                     .password(member.getPassword())
                                     .roles(member.getRole().name())
                                     .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(member, null, authoritiesMapper.mapAuthorities(userDetail.getAuthorities()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Refresh Token 값을 가진 사용자를 찾고, 존재하는 경우 HTTP 응답 객체 헤더에 새로운 Access Token을 세팅하는 함수.
     * <p>HTTP 요청을 통해 받은 Refresh Token 값을 가진 사용자({@link Member})가 존재하는지 확인하고,</p>
     * <p>존재할 경우 새로운 Access Token을 발급받아 HTTP 응답 객체 헤더에 Access Token 값을 세팅한다.</p>
     * @param response HTTP 응답 객체
     * @param refreshToken JWT Refresh Token
     */
    private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
                        .ifPresent(member -> jwtService.sendAccessToken(response, jwtService.createAccessToken(member.getUsername())));
    }
}
