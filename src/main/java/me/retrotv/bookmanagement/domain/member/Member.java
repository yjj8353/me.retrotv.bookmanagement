package me.retrotv.bookmanagement.domain.member;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.common.CommonEntity;
import me.retrotv.bookmanagement.util.EncryptUtil;

/**
 * 사용자 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MEMBER")
public class Member extends CommonEntity {

    /*
     * 사용자의 아이디 [UNIQUE, NOT NULL]
     */
    @Column(name = "MEMBER_USERNAME", unique = true, nullable = false, length = 20)
    private String username;

    /*
     * 사용자의 패스워드 [NOT NULL]
     */
    @Column(name = "MEMBER_PASSWORD", nullable = false)
    private String password;

    /*
     * 사용자의 실명
     */
    @Column(name = "MEMBER_REAL_NAME", length = 30)
    private String realName;

    /*
     * 사용자의 별명
     */
    @Column(name = "MEMBER_NICK_NAME", length = 30)
    private String nickName;

    /*
     * 사용자의 이메일
     */
    @Column(name = "MEMBER_EMAIL", unique = true, nullable = false)
    private String email;

    /*
     * 사용자의 권한
     */
    @Column(name = "MEMBER_ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /*
     * 사용자 인증 여부
     */
    @Column(name = "MEMBER_CERTIFIED")
    private boolean isCertified;

    /*
     * 사용자가 발급받은 JWT Refresh Token
     */
    @Column(name = "MEMBER_REFRESH_TOKEN")
    private String refreshToken;

    /*
     * 사용자 인증을 위한 패스코드
     */
    @Column(name = "MEMBER_PASSCODE")
    private String passcode;

    /*
     * 등록 시간
     */
    @Column(name = "MEMBER_REGIST_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime registDate;

    /*
     * 수정 시간
     */
    @Column(name = "MEMBER_UPDATE_DATE", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

    /**
     * 패스워드 수정을 위한 함수.
     * @param password 변경할 패스워드 문자열
     */
    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * 실명 수정을 위한 함수.
     * @param realName 변경할 실명 문자열
     */
    public void updateRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 별명 수정을 위한 함수.
     * @param nickName 변경할 별명 문자열
     */
    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 이메일 수정을 위한 함수.
     * @param email 변경할 이메일 문자열
     */
    public void updateEmail(String email) {
        this.email = email;
    }

    /**
     * passcode 생성 및 등록 함수.
     * @throws NoSuchAlgorithmException
     */
    public void createPasscode() throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstanceStrong();
        String uniqueString = this.username + this.email + String.valueOf(random.nextInt());
        this.passcode = EncryptUtil.SHA.sha256(uniqueString);
    }

    /**
     * JWT Refresh Token 수정을 위한 함수.
     * @param refreshToken 변경할 Refresh Token 문자열
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 발급받은 JWT Refresh Token 파기를 위한 함수.
     */
    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    public void destroyPasscode() {
        this.passcode = null;
    }

    /**
     * 계정 활성화 함수.
     */
    public void activateAccount() {
        this.isCertified = true;
    }

    /**
     * 계정 비활성화 함수.
     */
    public void deactivateAccount() {
        this.isCertified = false;
    }

    /**
     * 멤버 권한.
     */
    public enum Role {
        ADMIN,
        USER
    }
}
