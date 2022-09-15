package me.retrotv.bookmanagement.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.domain.jwt.JwtService;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.member.MemberService;
import me.retrotv.bookmanagement.response.BasicError;

/**
 * Spring Securit 설정을 위한 객체.
 * @version 1.0
 * @author yjj8353
 */
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {
    private final MemberService memberService;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    @Bean
    public WebSecurityCustomizer webSecurity() {
        return (web) -> web.ignoring().antMatchers("/", "/index.html", "/favicon.ico", "/icons/**", "/assets/**");
    }

    /**
     * 세부적인 보안설정을 위한 빈.
     * @param http 세부적인 보안설정을 위한 HttpSecurity 객체
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            // formLogin 인증방법 비활성화.
        http.formLogin()
            .disable()
            
            // httpBasic 인증방법 비활성화.
            .httpBasic()
            .disable()
            
            // csrf 검증 비활성화.
            .csrf()
            .disable()
            
            // JWT Token을 사용하기 때문에 세션 생성 정책을 STATELESS 하도록 설정한다.
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // 여기에 기술한 URI를 제외한 나머지는 모두 인증이 필요하도록 설정한다.
            .authorizeRequests()
            .antMatchers("/api/member/login", "/api/member/join", "/api/member/logout", "/api/member/certify", "/api/member/password-change-none-auth", "/api/member/password-change-email-send", "/api/book/search", "/api/image/download", "/api/jwt/valid").permitAll()
            .anyRequest().authenticated();
        
        // Filter 순서에 주의, After와 Before 둘이 뒤바뀌면 실행이 안됨.
        http.addFilterAfter(jsonUsernamePasswordLoginFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), JsonUsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint());

        return http.build();
    }

    /**
     * 패스워드 암호화시 사용하는 패스워드 인코더 빈(Bean) 객체.
     * @return {@link PasswordEncoder} 객체
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 인증절차 과정을 일부 변경하기 위해 사용하는 빈(Bean) 객체.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberService);
        provider.setPasswordEncoder(passwordEncoder());

        /* 
         * hideUserNotFoundExceptions 값을 false로 설정하지 않으면 UsernameNotFoundException을 반환할 때, 강제로 BadCredentialsException로 Throw 한다.
         * 내부적으로 유저 ID가 없을 경우와, 패스워드가 틀렸을 경우의 처리를 별도로 하고 싶다면 해당 값을 false로 설정해야 한다.
         * 단, frontend에 리턴 할 때는, 두가지 오류를 분류하지 않고 하나로 처리하는 것이 보안상 유리하다.
         */
        provider.setHideUserNotFoundExceptions(false);
        return new ProviderManager(provider);
    }

    /**
     * 로그인 성공시 부가작업을 위한 핸들러 빈(Bean) 객체.
     * @return 로그인 성공 핸들러 객체
     */
    @Bean
    LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler() {
        return new LoginSuccessJWTProvideHandler(jwtService, memberService, memberRepository);
    }

    /**
     * 로그인 실패 핸들러 빈(Bean) 객체.
     * @return 로그인 실패 핸들러 객체
     */
    @Bean
    LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    /**
     * 사용자 로그인 처리를 위한 필터를 설정하는 빈(Bean) 객체.
     * @return 사용자 로그인 필터 객체
     */
    @Bean
    JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() {
        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper, memberRepository);
        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonUsernamePasswordLoginFilter;
    }

    /**
     * JWT 인증 필터를 설정하는 빈(Bean) 객체.
     * @return JWT 인증 필터 객체
     */
    @Bean
    JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
    }

    /**
     * 인증과정 실패를 설정하는 빈(Bean) 객체.
     * @return 인증 엔트리 포인트 객체
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {

            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                BasicError result = new BasicError(authException.getMessage(), null, HttpStatus.UNAUTHORIZED);
                String resultString = objectMapper.writeValueAsString(result);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(resultString);
            }
        };
    }
}
