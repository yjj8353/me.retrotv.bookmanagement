package me.retrotv.bookmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * {@link RequestLoggerFilter}의 설정을 위한 객체
 * @version 1.0
 * @author yjj8353
 */
@Configuration
public class RequestLoggerFilter {
    
    /**
     * API 호출시 표시되는 로그의 세부적인 표시여부를 설정하는 빈(Bean) 객체.
     * @return 설정이 변경된 {@link CommonsRequestLoggingFilter} 객체
     */
    @Bean
    CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        /*
         * XML로 설정할 경우
         * <bean id="commonsRequestLoggingFilter" class="org.springframework.web.filter.CommonsRequestLoggingFilter">
         *     <property name="includeClientInfo" value="false" />
         *     <property name="includeHeaders" value="false" />
         *     <property name="includePayload" value="true" />
         *     <property name="includeQueryString" value="true" />
         *     <property name="maxPayloadLength" value="10000" />
         * </bean>
         * 
         * web.xml
         * <filter>
         *     <filter-name>commonsRequestLoggingFilter</filter-name>
         *     <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
         * </filter>
         * <filter-mapping>
         *     <filter-name>commonsRequestLoggingFilter</filter-name>
         *     <url-pattern>/*</url-pattern>
         * </filter-mapping>
         * 
         * logback.xml
         * <logger name="org.springframework.web.filter" level="debug" />
         */

        /*
         * setIncludeClientInfo(true) : 클라이언트 주소와 세션 ID를 로그 메세지에 포함한다.
         * setIncludeHeaders(true)    : 헤더정보를 로그에 포함한다.
         * setIncludePayload(true)    : request내용을 로그에 포함한다.
         * setIncludeQueryString(true): 쿼리 문자열을 로그 메세지에 포함한다.
         * setMaxPayloadLength(1000)  : 로그의 최대 길이을 설정한다.
         */

        filter.setIncludeClientInfo(false);
        filter.setIncludeHeaders(false);
        filter.setIncludePayload(true);
        filter.setIncludeQueryString(true);
        filter.setMaxPayloadLength(10000);
        
        return filter;
    }
}
