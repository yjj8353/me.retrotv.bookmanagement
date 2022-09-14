package me.retrotv.bookmanagement.domain.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 유저({@link Member}) 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface MemberRepository extends JpaRepository<Member, String>, JpaSpecificationExecutor<Member> {

    /**
     * 유저의 ID로 한명의 유저 정보를 되돌려주는 함수.
     * @param username 유저의 ID
     * @return 유저 한명의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Member> findByUsername(String username);

    /**
     * 유저의 이메일로 한명의 유저 정보를 되돌려주는 함수.
     * @param email 유저의 이메일
     * @return 유저 한명의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Member> findByEmail(String email);

    /**
     * 유저의 ID로 한명의 유저 정보를 되돌려주는 함수.
     * @param username 유저의 ID
     * @param passcode 해당 유저의 인증을 위한 패스코드
     * @return 유저 한명의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Member> findByUsernameAndPasscode(String username, String passcode);

    /**
     * 유저가 발급받은 JWT Refresh Token 값으로 한명의 유저 정보를 되돌려주는 함수.
     * @param refreshToken 유저가 발급받은 JWT Refresh Token 값
     * @return 유저 한명의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Member> findByRefreshToken(String refreshToken);

    @Query(value = "SELECT EXISTS (SELECT * FROM MEMBER);", nativeQuery = true)
    boolean existsMemeber();

    List<Member> findByIsCertifiedFalse();
}
