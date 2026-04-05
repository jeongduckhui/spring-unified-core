package com.example.demo.soap.config;

import com.example.demo.soap.core.SoapClientInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SoapProperties.class)
public class SoapConfig {

    /**
     * application.properties 의 app.soap.* 설정값 바인딩 객체
     *
     * 예:
     * - 승인 SOAP endpoint
     * - 결제 SOAP endpoint
     * - connection/read timeout
     * - SOAP Header 관련 설정
     */
    private final SoapProperties soapProperties;

    /**
     * SOAP 공통 인터셉터
     *
     * 역할:
     * - 요청 직전 Header 추가
     * - 최종 요청 XML 로그 출력
     * - SOAP Fault 로그 출력
     */
    private final SoapClientInterceptor soapClientInterceptor;

    /**
     * JAXB 마샬러/언마샬러 등록
     *
     * 역할:
     * - Java 객체 → XML 변환
     * - XML → Java 객체 변환
     *
     * setPackagesToScan("com.example.demo.soap")
     *   → 해당 패키지 하위의 JAXB DTO 스캔
     *   → @XmlRootElement, @XmlType, @XmlElement 등이 붙은 클래스 대상
     *
     * 예:
     * - SoapApproveRequest
     * - SoapApproveResponse
     * - SoapPaymentRequest
     * - SoapPaymentResponse
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.example.demo.soap");
        return marshaller;
    }

    /**
     * 승인 SOAP 전용 WebServiceTemplate
     *
     * approvalTemplate 을 별도 Bean 으로 분리한 이유:
     * - 승인 시스템 endpoint 를 독립적으로 관리하기 위함
     * - 향후 승인 시스템만 별도 timeout/interceptor 정책 적용 가능
     */
    @Bean
    @Qualifier("approvalTemplate")
    public WebServiceTemplate approvalTemplate(Jaxb2Marshaller marshaller) {
        return createTemplate(
                marshaller,
                soapProperties.getApproval().getUri()
        );
    }

    /**
     * 결제 SOAP 전용 WebServiceTemplate
     *
     * paymentTemplate 을 별도 Bean 으로 분리한 이유:
     * - 결제 시스템 endpoint 를 독립적으로 관리하기 위함
     * - 향후 결제 시스템만 별도 설정 적용 가능
     */
    @Bean
    @Qualifier("paymentTemplate")
    public WebServiceTemplate paymentTemplate(Jaxb2Marshaller marshaller) {
        return createTemplate(
                marshaller,
                soapProperties.getPayment().getUri()
        );
    }

    /**
     * SOAP Template 공통 생성
     *
     * WebServiceTemplate 는 Spring-WS 에서 SOAP 호출의 중심 객체다.
     *
     * 이 메서드에서 설정하는 내용:
     * 1. marshaller / unmarshaller 등록
     * 2. 기본 endpoint URI 설정
     * 3. HTTP message sender + timeout 설정
     * 4. SOAP 공통 interceptor 연결
     *
     * 호출 흐름:
     * DTO
     * → marshal
     * → interceptor.handleRequest()
     * → Header 추가
     * → HTTP 전송
     * → 응답 수신
     * → interceptor.handleResponse() 또는 handleFault()
     * → unmarshal
     */
    private WebServiceTemplate createTemplate(Jaxb2Marshaller marshaller, String defaultUri) {

        /**
         * SOAP 호출 실행 객체 생성
         */
        WebServiceTemplate template = new WebServiceTemplate();

        /**
         * 요청 객체를 XML 로 변환할 때 사용
         *
         * 예:
         * SoapApproveRequest → <approveRequest> XML
         */
        template.setMarshaller(marshaller);

        /**
         * 응답 XML 을 Java 객체로 변환할 때 사용
         *
         * 예:
         * <approveResponse> XML → SoapApproveResponse
         */
        template.setUnmarshaller(marshaller);

        /**
         * 기본 SOAP endpoint 주소 설정
         *
         * 예:
         * http://localhost:8081/ws
         *
         * 의미:
         * 실제 SOAP 요청을 전송할 대상 주소
         */
        template.setDefaultUri(defaultUri);

        /**
         * 실제 HTTP 통신 담당 객체
         *
         * Spring-WS 의 SOAP 메시지를 HTTP 로 전송하는 역할
         */
        HttpComponentsMessageSender sender = new HttpComponentsMessageSender();

        /**
         * 연결 타임아웃
         *
         * 지정 시간 내 서버 연결이 안 되면 실패 처리
         */
        sender.setConnectionTimeout(soapProperties.getTimeout().getConnection());

        /**
         * 응답 대기 타임아웃
         *
         * 연결은 되었지만 지정 시간 내 응답이 오지 않으면 실패 처리
         */
        sender.setReadTimeout(soapProperties.getTimeout().getRead());

        /**
         * WebServiceTemplate 에 HTTP 전송기 등록
         */
        template.setMessageSender(sender);

        /**
         * SOAP 공통 인터셉터 등록
         *
         * handleRequest:
         * - SOAP Header 추가
         * - 최종 요청 XML 로그
         *
         * handleResponse:
         * - 현재 별도 처리 없음
         *
         * handleFault:
         * - SOAP Fault XML 로그
         */
        template.setInterceptors(new ClientInterceptor[]{
                soapClientInterceptor
        });

        return template;
    }
}