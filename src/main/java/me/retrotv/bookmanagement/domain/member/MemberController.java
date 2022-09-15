package me.retrotv.bookmanagement.domain.member;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.domain.member.Member.Role;
import me.retrotv.bookmanagement.response.BasicResult;

/**
 * 사용자({@link Member}) 컨트롤러 계층.
 * @version 1.0
 * @author yjj8353
 */
@ResponseBody
@RestController
@RequiredArgsConstructor
@RequestMapping("api/member")
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원가입 요청을 처리하는 함수.
     * @return 회원가입 결과값이 담긴 ResponseEntity 객체
     */
    @PostMapping("/join")
    public ResponseEntity<BasicResult> join(@Valid @RequestBody MemberDTO memberDTO) {
        MemberDTO newMemberDTO = MemberDTO.builder()
                                          .id(null)
                                          .username(memberDTO.getUsername())
                                          .password(memberDTO.getPassword())
                                          .realName(memberDTO.getRealName())
                                          .email(memberDTO.getEmail())
                                          .role(Role.ADMIN)
                                          .build();

        BasicResult result = memberService.saveMember(newMemberDTO);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 로그아웃 요청을 처리하는 함수.
     * @return 로그아웃 결과값이 담긴 ResponseEntity 객체
     */
    @PostMapping("/logout")
    public ResponseEntity<BasicResult> logout(@Valid @RequestBody MemberDTO memberDTO) {
        BasicResult result = memberService.logoutMember(memberDTO.getRefreshToken().replace("Bearer ", ""));
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/not-certified")
    public ResponseEntity<BasicResult> getNotCertifiedMemebers() {
        BasicResult result = memberService.getNotCertifiedMemebers();
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/certify")
    public ResponseEntity<BasicResult> certifyMember(@RequestParam("username") String username, @RequestParam("passcode") String passcode) {
        BasicResult result = memberService.certifyMember(username, passcode);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/password-change-with-auth")
    public ResponseEntity<BasicResult> passwordChangeWithAuth(@Valid @RequestBody MemberDTO.ChangePassword memberDTO) {
        BasicResult result = memberService.changePassword(memberDTO);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/password-change-none-auth")
    public ResponseEntity<BasicResult> passwordChangeNoneAuth(@Valid @RequestBody MemberDTO.ChangePassword memberDTO) {
        BasicResult result = memberService.changePassword(memberDTO);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/password-change-email-send")
    public ResponseEntity<BasicResult> passwordChangeEmailSend(@RequestParam("email") String email) {
        BasicResult result = memberService.passwordChangeEmailSend(email);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
