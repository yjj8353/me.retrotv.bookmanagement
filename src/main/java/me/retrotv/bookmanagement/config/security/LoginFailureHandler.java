package me.retrotv.bookmanagement.config.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.exception.NotCertificatedException;
import me.retrotv.bookmanagement.response.BasicError;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 로그인 실패를 제어하기 위한 핸들러 객체.
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    /**
     * * SimpleUrlAuthenticationFailureHandler 클래스 로부터 상속받음.
     * <p>인증관련 예외 발생시, 해당 예외를 처리하는 함수.</p>
     * <p>여기서는 해당하는 계정이 없을시 발생하는 {@link UsernameNotFoundException}예외와</p>
     * <p>패스워드가 틀릴시 발생하는 {@link BadCredentialsException}예외를 처리한다.</p>
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param exception 인증관련 예외가 담긴 객체
     * @exception IOException
     * @exception ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        BasicError result = new BasicError(null, null, HttpStatus.UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        /*
         * UsernameNotFoundException: username에 해당하는 계정이 없음
         * BadCredentialsException: 패스워드가 틀림
         */
        if(exception instanceof UsernameNotFoundException || exception instanceof BadCredentialsException) {

            /*
             * 로직상 계정이 틀렸을 경우와 비밀번호가 틀렸을 경우 별도의 처리를 하더라도,
             * 클라이언트에 값을 돌려줄 때에는 하나의 에러로만 response로 전송해야 한다 (보안문제)
             */
            log.debug("아이디 혹은 패스워드가 틀림");
            result.setMessage("아이디 혹은 패스워드가 틀렸습니다.");
            response.setStatus(result.getStatus().value());
            response.getWriter().write(result.toJsonString());
        } else if(exception instanceof NotCertificatedException) {
            log.debug("인증되지 않은 사용자");
            result.setMessage("인증되지 않은 사용자 입니다.\n이메일 인증이 필요합니다.");
            response.setStatus(result.getStatus().value());
            response.getWriter().write(result.toJsonString());
        }
    }
}