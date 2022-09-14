package me.retrotv.bookmanagement.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.domain.jwt.JwtService;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.member.MemberService;

/**
 * 로그인 성공시 부가작업을 위한 핸들러 객체.
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtUtil;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    LoginSuccessJWTProvideHandler(JwtService jwtUtil, MemberService memberService, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    /**
     * * SimpleUrlAuthenticationSuccessHandler 클래스 로부터 상속받음.
     * <p>로그인 성공시 실행되는 함수.</p>
     * <p>Access Token과 Refresh Token을 발급해 HTTP 응답 헤더에 세팅하고, 로그인 성공한 유저의 {@link Member} 엔티티 객체에 Refresh Token 값을 업데이트한다.</p>
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 정보 객체
     * @exception IOException
     * @exception ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = extractUsername(authentication);
        String accessToken = jwtUtil.createAccessToken(username);
        String refreshToken = jwtUtil.createRefreshToken();

        log.debug("username: " + username);

        jwtUtil.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        memberRepository.findByUsername(username)
                        .ifPresent(member -> memberService.updateRefreshToken(member.getUsername(), refreshToken));
    }

    /**
     * 인증 정보 객체에서 유저의 ID를 추출해 반환하는 함수.
     * @param authentication 인증 정보 객체
     * @return 로그인한 유저의 ID
     */
    private String extractUsername(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
