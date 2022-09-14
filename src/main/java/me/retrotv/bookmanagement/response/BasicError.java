package me.retrotv.bookmanagement.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link BasicResult}를 상속받아 {@link ResponseEntity} 본문(Body)에 포함되는 규격화된 에러 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Setter
public class BasicError extends BasicResult {
    private boolean success = false;
    private String message = "원인불명의 에러가 발생했습니다.\n이것은 당신의 문제가 아닙니다!\n지속적으로 문제가 발생할 경우 관리자에게 연락해 주세요.";
    private Object data = null;
    private int count = 0;
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>message = "원인불명의 에러가 발생했습니다."</p>
     * <p>data = null</p>
     * <p>count = 0</p>
     * <p>status = HttpStatus.INTERNAL_SERVER_ERROR</p>
     */
    public BasicError() {
        super();
    }

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>data = null</p>
     * <p>count = 0</p>
     * <p>status = HttpStatus.INTERNAL_SERVER_ERROR</p>
     * @param message 반환할 에러 메시지
     */
    public BasicError(String message) {
        this.message = message;
    }

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>count = 1</p>
     * <p>status = HttpStatus.INTERNAL_SERVER_ERROR</p>
     * @param message 반환할 에러 메시지
     * @param data 반환할 데이터
     */
    public BasicError(String message, Object data) {
        this.message = message;
        this.data = data;
        this.count = 1;
    }

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>status = HttpStatus.INTERNAL_SERVER_ERROR</p>
     * @param message 반환할 에러 메시지
     * @param data 반환할 데이터
     * @param count data의 개수
     */
    public BasicError(String message, Object data, int count) {
        this.message = message;
        this.data = data;
        this.count = count;
    }

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>count = 1</p>
     * <p>status = HttpStatus.INTERNAL_SERVER_ERROR</p>
     * @param message 반환할 에러 메시지
     * @param data 반환할 데이터
     * @param status 반환할 에러 HTTP Status
     */
    public BasicError(String message, Object data, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.count = 1;
        this.status = status;
    }

    /**
     * {@link BasicError} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = false</p>
     * <p>data = null</p>
     * <p>count = 0</p>
     * @param message 반환할 에러 메시지
     * @param status 반환할 에러 HTTP Status
     */
    public BasicError(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
