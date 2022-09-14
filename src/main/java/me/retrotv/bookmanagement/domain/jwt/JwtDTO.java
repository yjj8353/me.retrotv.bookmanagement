package me.retrotv.bookmanagement.domain.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.validator.Username;

/**
 * JWT Token 전송 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtDTO {

    /*
     * Member의 username
     */
    @Username
    private String username;

    /*
     * JWT Access Token
     */
    private String accessToken;

    /*
     * JWT Refresh Token
     */
    private String refreshToken;
}
