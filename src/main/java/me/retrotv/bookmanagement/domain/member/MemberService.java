package me.retrotv.bookmanagement.domain.member;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.exception.CertifyFailException;
import me.retrotv.bookmanagement.exception.CommonServerErrorException;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;
import me.retrotv.bookmanagement.util.MailUtil;

/**
 * 사용자({@link Member}) 서비스 계층.
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Value("${spring.profiles.active}")
	private String springProfile;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    /**
     * Spring Security에서 유저정보를 가져오기 위해 사용하는 함수.
     * @param username 유저 ID
     * @return Spring Security에서 사용하기 위해 가공된 {@link UserDetails} 객체
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("아이디가 없습니다"));
        
        return User.builder()
                   .username(member.getUsername())
                   .password(member.getPassword())
                   .roles(member.getRole().toString())
                   .build();
    }

    /**
     * 사용자 가입 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param memberDTO 가입요청할 유저의 정보가 담긴 {@link MemberDTO} 객체
     * @return 회원가입 결과를 담은 {@link BasicResult} 객체
     */
    @Transactional
    public BasicResult saveMember(MemberDTO memberDTO) {
        boolean duplicate = memberRepository.findByUsername(memberDTO.getUsername()).isPresent();
        log.debug("계정 중복 여부: {}", duplicate);

        if(duplicate) {
            log.debug("계정이 중복됨");
            return new BasicError("이미 존재하는 계정 입니다.", HttpStatus.CONFLICT); 
        }

        duplicate = memberRepository.findByEmail(memberDTO.getEmail()).isPresent();
        log.debug("이메일 중복 여부: {}", duplicate);

        if(duplicate) {
            log.debug("이메일이 중복됨");
            return new BasicError("이미 사용중인 이메일 입니다.", HttpStatus.CONFLICT); 
        }

        Member member = modelMapper.map(memberDTO, Member.class);
        member.updatePassword(delegatingPasswordEncoder.encode(member.getPassword()));

        try {
            member.createPasscode();
        } catch (NoSuchAlgorithmException e) {
            throw new CommonServerErrorException("MemberService: saveMember 함수에서 NoSuchAlgorithmException 발생");
        }

        log.warn("프로필: " + springProfile);

        if("prod".equals(springProfile)) {
            String username = member.getUsername();
            String passcode = member.getPasscode();
            String address = member.getEmail();
            String subject = "가입 인증용 메일입니다";
            String text = "<p>다음의 링크를 클릭하면 가입이 완료됩니다.</p>"
                        + String.format("<a href='https://bookmanagement.retrotv.me:8443/#/certify?username=%s&passcode=%s'>https://bookmanagement.retrotv.me:8443/#/certify?username=%s&passcode=%s</a>", username, passcode, username, passcode);

            log.debug("이메일 내용: {}", text);

            MailUtil.sendMail(address, subject, text);
        } else {
            member.activateAccount();
        }
        
        memberRepository.save(member);

        BasicResult result = null;
        if(member.getUsername() != null && member.getUsername().equals(memberDTO.getUsername())) {
            result = new BasicResult("회원가입 성공!\n인증을 위해 이메일을 확인해 주세요.");
        } else {
            result = new BasicError("원인불명의 이유로 회원가입에 실패했습니다.");
        }

        return result;
    }

    /**
     * 사용자 JWT Refresh Token 수정 요청을 받아 처리하는 함수.
     * @param username 수정할 사용자의 ID
     * @param refreshToken 수정할 Refresh Token 문자열
     */
    @Transactional
    public void updateRefreshToken(String username, String refreshToken) {
        memberRepository.findByUsername(username).ifPresent(member -> member.updateRefreshToken(refreshToken));
    }

    /**
     * 사용자의 로그아웃 요청을 받아 처리하는 함수.
     * @param refreshToken 로그아웃 할 유저의 Refresh Token 값
     * @return 로그아웃 결과를 담은 {@link BasicResult} 객체
     */
    @Transactional
    public BasicResult logoutMember(String refreshToken) {
        Member member = memberRepository.findByRefreshToken(refreshToken).orElse(null);

        if(member != null) {
            member.destroyRefreshToken();
        }

        return new BasicResult("정상적으로 로그아웃 처리 되었습니다.");
    }

    @Transactional
    public BasicResult getNotCertifiedMemebers() {
        List<Member> members = memberRepository.findByIsCertifiedFalse();
        List<MemberDTO.Return> memberDTOs = new ArrayList<>();
        members.forEach(member -> 
            memberDTOs.add(modelMapper.map(member, MemberDTO.Return.class))
        );

        return new BasicResult(null, memberDTOs, memberDTOs.size());
    }

    @Transactional
    public BasicResult certifyMember(String username, String passcode) {
        if(username == null || passcode == null) {
            throw new CertifyFailException("인증에 실패 했습니다.");
        }

        Member member = memberRepository.findByUsernameAndPasscode(username, passcode)
                                        .orElseThrow(() -> new CertifyFailException("인증에 실패 했습니다."));
        member.activateAccount();
        member.destroyPasscode();

        log.debug("활성화 완료");
        return new BasicResult(member.getRealName() + "님, 정상적으로 인증 되었습니다.");
    }
}
