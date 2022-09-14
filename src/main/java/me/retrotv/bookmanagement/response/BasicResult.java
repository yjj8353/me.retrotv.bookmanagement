package me.retrotv.bookmanagement.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link ResponseEntity} 본문(Body)에 포함되는 규격화된 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Setter
public class BasicResult {
    private boolean success = true;
    private String message = "요청한 작업이 정상적으로 완료 되었습니다.";
    private Object data = null;
    private int count = 0;
    private HttpStatus status = HttpStatus.OK;

    /**
     * {@link BasicResult} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = true</p>
     * <p>message = "요청한 작업이 정상적으로 완료 되었습니다."</p>
     * <p>data = null</p>
     * <p>count = 0</p>
     * <p>tatus = HttpStatus.OK</p>
     */
    public BasicResult() {
        super();
    }

    /**
     * {@link BasicResult} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = true</p>
     * <p>data = null</p>
     * <p>count = 0</p>
     * <p>tatus = HttpStatus.OK</p>
     * @param message 반환할 메시지
     */
    public BasicResult(String message) {
        this.message = message;
    }

    /**
     * {@link BasicResult} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = true</p>
     * <p>count = 1</p>
     * <p>tatus = HttpStatus.OK</p>
     * @param message 반환할 메시지
     * @param data 반환할 데이터
     */
    public BasicResult(String message, Object data) {
        this.message = message;
        this.data = data;
        this.count = 1;
    }

    /**
     * {@link BasicResult} 기본 생성자.
     * <p>기본적으로 세팅되는 값.</p>
     * <p>success = true</p>
     * <p>tatus = HttpStatus.OK</p>
     * @param message 반환할 메시지
     * @param data 반환할 데이터
     * @param count data의 개수
     */
    public BasicResult(String message, Object data, int count) {
        this.message = message;
        this.data = data;
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCatedData(Object obj) {
        return (T) obj;
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
