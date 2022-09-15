package me.retrotv.bookmanagement.domain.member;

import javax.validation.constraints.Email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.member.Member.Role;
import me.retrotv.bookmanagement.validator.Username;

/**
 * 유저 데이터 전송 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

    /*
     * 식별을 위한 고유 ID 값
     */
    private Long id;

    /*
     * 사용자의 아이디
     */
    @Username
    private String username;

    /*
     * 사용자의 패스워드
     */
    private String password;
    
    /*
     * 가입시 사용자의 패스워드를 체크를 위한 값
     */
    private String passwordCheck;

    /*
     * 사용자의 실명
     */
    private String realName;

    /*
     * 사용자의 별명
     */
    private String nickName;

    /*
     * 사용자의 Refresh Token
     */
    private String refreshToken;

    /*
     * 사용자의 이메일
     */
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    /*
     * 사용자의 권한
     */
    private Role role;

    /**
     * 패스워드/토큰 데이터를 제외한 유저 데이터 전송 객체
     * @version 1.0
     * @author yjj8353
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Return {
        
        /*
         * 식별을 위한 고유 ID 값
         */
        private Long id;

        /*
         * 사용자의 아이디
         */
        @Username
        private String username;

        /*
         * 사용자의 실명
         */
        private String realName;

        /*
         * 사용자의 별명
         */
        private String nickName;

        /*
         * 사용자의 이메일
         */
        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        /*
         * 사용자의 권한
         */
        private Role role;
    }

    /**
     * 패스워드 변경을 위한 유저 데이터 전송 객체
     * @version 1.0
     * @author yjj8353
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePassword {

        /*
         * 식별을 위한 고유 ID 값
         */
        private Long id;

        /*
         * 사용자의 아이디
         */
        @Username
        private String username;

        /*
         * 사용자의 기존 패스워드
         */
        private String orgPassword;

        /*
         * 사용자의 새로운 패스워드
         */
        private String newPassword;

        /*
         * 사용자의 새로운 패스워드 체크용
         */
        private String newPasswordCheck;

        /*
         * 사용자의 Refresh Token
         */
        private String refreshToken;

        /*
         * 패스워드 변경 시 사용자를 식별하기 위한 위한 패스코드
         */
        private String passcode;
    }
}
