package me.retrotv.bookmanagement.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.validation.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.exception.CertifyFailException;
import me.retrotv.bookmanagement.exception.CommonServerErrorException;
import me.retrotv.bookmanagement.exception.NoSuchApiElementException;
import me.retrotv.bookmanagement.exception.PasswordChangeFailException;
import me.retrotv.bookmanagement.exception.XmlParseErrorException;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;

/**
 * 전역 예외 처리 핸들러
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_COMMON_MESSAGE = "요청을 처리할 수 없습니다.\n증상이 계속될 경우, 관리자에게 연락하시기 바랍니다.";
    
    /**
     * NoSuchElementException을 공통으로 처리하는 함수
     * @param exception
     * @return 오류 정보가 담긴 담긴 ResponseEntity 객체
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<BasicResult> handle(NoSuchElementException exception) {
        log.debug(exception.getMessage());
        BasicError result = new BasicError(ERROR_COMMON_MESSAGE, null, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * FileNotFoundException을 공통으로 처리하는 함수
     * @param exception
     * @return 오류 정보가 담긴 담긴 ResponseEntity 객체
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<BasicResult> handle(FileNotFoundException exception) {
        log.debug("저장소에 저장된 이미지 파일이 없습니다.");
        BasicError result = new BasicError(ERROR_COMMON_MESSAGE, null, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * MethodArgumentNotValidException을 공통으로 처리하는 함수
     * @param exception
     * @return 오류 정보가 담긴 담긴 ResponseEntity 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResult> handle(MethodArgumentNotValidException exception) {
        log.debug("유효성 검사에 실패했습니다.");
        BasicError result = new BasicError(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage(), null, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * ValidationException 공통으로 처리하는 함수
     * @param exception
     * @return 오류 정보가 담긴 담긴 ResponseEntity 객체
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BasicResult> handle(ValidationException exception) {
        log.debug("유효성 검사에 실패했습니다.");
        BasicError result = new BasicError(exception.getMessage(), null, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<BasicResult> handle(IOException exception) {
        log.debug("IOException이 발생했습니다.");
        BasicError result = new BasicError(exception.getMessage());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<BasicResult> handle(HttpClientErrorException exception) {
        log.debug("HttpClientErrorException이 발생했습니다.");
        BasicError result = new BasicError("잘못된 ISBN 값을 입력한 것 같습니다.\nISBN을 값을 확인해 주세요", exception.getStatusCode());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(NoSuchApiElementException.class)
    public ResponseEntity<BasicResult> handle(NoSuchApiElementException exception) {
        log.debug("NoSuchApiElementException이 발생했습니다.");
        BasicResult result = new BasicResult(exception.getMessage());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(XmlParseErrorException.class)
    public ResponseEntity<BasicResult> handle(XmlParseErrorException exception) {
        log.debug("XML 파싱 예외가 발생했습니다.");
        BasicError result = new BasicError(exception.getMessage());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(CertifyFailException.class)
    public ResponseEntity<BasicResult> handle(CertifyFailException exception) {
        log.debug("인증 실패 예외가 발생했습니다.");
        BasicError result = new BasicError(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(CommonServerErrorException.class)
    public ResponseEntity<BasicResult> handle(CommonServerErrorException exception) {
        log.debug(exception.getMessage());
        BasicError result = new BasicError();
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @ExceptionHandler(PasswordChangeFailException.class)
    public ResponseEntity<BasicResult> handle(PasswordChangeFailException exception) {
        log.debug("패스워드 변경 실패 예외가 발생했습니다.");
        BasicError result = new BasicError(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
