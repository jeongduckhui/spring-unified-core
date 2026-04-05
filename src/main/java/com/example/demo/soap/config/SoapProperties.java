package com.example.demo.soap.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.soap")
public class SoapProperties {

    /**
     * 승인 SOAP 서버 endpoint
     */
    private Endpoint approval = new Endpoint();

    /**
     * 결제 SOAP 서버 endpoint
     */
    private Endpoint payment = new Endpoint();

    /**
     * 공통 HTTP timeout
     */
    private Timeout timeout = new Timeout();

    /**
     * 공통 SOAP Header 설정
     */
    private Header header = new Header();

    @Getter
    @Setter
    public static class Endpoint {
        private String uri;
    }

    @Getter
    @Setter
    public static class Timeout {
        /**
         * connection timeout (ms)
         */
        private int connection = 3000;

        /**
         * read timeout (ms)
         */
        private int read = 5000;
    }

    @Getter
    @Setter
    public static class Header {
        /**
         * SOAP Header namespace
         */
        private String namespace = "http://example.com/auth";

        /**
         * SOAP Header prefix
         */
        private String prefix = "auth";

        /**
         * 시스템 식별자
         */
        private String systemId = "SYSTEM_001";

        /**
         * 외부 연동 인증 토큰
         */
        private String authToken = "TEMP_TOKEN";
    }
}