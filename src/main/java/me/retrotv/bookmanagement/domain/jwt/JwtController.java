package me.retrotv.bookmanagement.domain.jwt;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;

@Slf4j
@ResponseBody
@RestController
@RequiredArgsConstructor
@RequestMapping("api/jwt")
public class JwtController {
    private final JwtService jwtService;

    @PostMapping("valid")
    public ResponseEntity<BasicResult> checkTokenValid(@Valid @RequestBody JwtDTO jwtDTO) {
        log.debug("JWT Refresh Token: {}", jwtDTO.getRefreshToken());

        String refreshToken = jwtDTO.getRefreshToken();
        String username = jwtService.findMemberByRefreshToken(refreshToken);
        boolean isValid = jwtService.isTokenValid(refreshToken) && (jwtService.findMemberByRefreshToken(refreshToken) != null);
        BasicResult result = null;

        log.debug("유효한 토큰: {}", isValid);

        if(isValid) {

            // HttpServletResponse 객체를 불러와, Accesss Token 값을 세팅한다.
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            String newAccessToken = jwtService.createAccessToken(username);
            String newRefreshToken = jwtService.createRefreshToken();
            
            jwtService.updateRefreshToken(username, newRefreshToken);
            jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

            Map<String, Boolean> map = new HashMap<>();
            map.put("isValid", true);
            result = new BasicResult(null, map);
        } else {
            jwtService.destroyRefreshToken(username, refreshToken);
            Map<String, Boolean> map = new HashMap<>();
            map.put("isValid", false);
            result = new BasicError(null, map, HttpStatus.UNAUTHORIZED);
        }

        log.debug("HTTP 응답: {}", result.getStatus().name());

        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
