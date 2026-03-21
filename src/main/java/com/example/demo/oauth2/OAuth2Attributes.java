package com.example.demo.oauth2;

import java.util.Map;

/**
 * OAuth2 / OIDC Provider 사용자 Attribute 공통 모델
 *
 * OAuth2 Provider마다 사용자 정보(attribute) 구조가 다르기 때문에
 * 이를 하나의 공통 객체로 변환하기 위한 클래스이다.
 *
 * 예
 *
 * Google attributes
 *
 * {
 *   "sub": "123456789",
 *   "email": "user@gmail.com",
 *   "name": "홍길동"
 * }
 *
 * Keycloak attributes
 *
 * {
 *   "sub": "abcd-1234",
 *   "email": "user@test.com",
 *   "preferred_username": "testuser"
 * }
 *
 * Provider마다 attribute 이름이 다르기 때문에
 * 이 클래스를 통해 공통 모델로 변환한다.
 *
 * 변환 결과
 *
 * provider
 * providerSubject
 * email
 * name
 *
 * 이 객체는 이후
 *
 * CustomOAuth2UserService
 * CustomOidcUserService
 *
 * 에서 내부 사용자(User) 생성 및 조회에 사용된다.
 */
public record OAuth2Attributes(

        /**
         * OAuth2 / OIDC Provider 이름
         *
         * 예
         *
         * GOOGLE
         * KEYCLOAK
         */
        String provider,

        /**
         * Provider 사용자 고유 식별자
         *
         * 대부분 Provider에서는
         *
         * sub (subject)
         *
         * 값을 사용자 고유 ID로 사용한다.
         *
         * 내부 시스템에서는
         *
         * provider + providerSubject
         *
         * 조합으로 외부 계정을 식별한다.
         */
        String providerSubject,

        /**
         * 사용자 이메일
         *
         * 내부 사용자 계정 매핑 시 사용된다.
         */
        String email,

        /**
         * 사용자 이름
         *
         * Provider에서 제공하는 사용자 표시 이름
         */
        String name
) {

    /**
     * Provider별 attribute 변환 Entry Point
     *
     * registrationId는
     *
     * application.yml 설정에 정의된
     * OAuth2 Client 이름이다.
     *
     * 예
     *
     * google
     * keycloak
     */
    public static OAuth2Attributes of(String registrationId, Map<String, Object> attributes) {

        /**
         * Provider 이름을 기준으로
         * attribute 변환 메서드를 선택한다.
         */
        return switch (registrationId.toLowerCase()) {

            /**
             * Google OAuth2 로그인
             */
            case "google" -> ofGoogle(attributes);

            /**
             * Keycloak OIDC 로그인
             */
            case "keycloak" -> ofKeycloak(attributes);

            /**
             * 지원하지 않는 Provider
             */
            default -> throw new IllegalArgumentException("Unsupported registrationId: " + registrationId);
        };
    }

    /**
     * Google OAuth2 사용자 정보 변환
     */
    private static OAuth2Attributes ofGoogle(Map<String, Object> attributes) {

        /**
         * Google 사용자 고유 ID
         *
         * Google OAuth2에서는
         * "sub" 필드가 사용자 고유 ID이다.
         */
        String sub = getRequiredString(attributes, "sub");

        /**
         * 사용자 이메일
         */
        String email = getRequiredString(attributes, "email");

        /**
         * 사용자 이름
         *
         * name 필드가 없을 경우
         * email을 기본값으로 사용한다.
         */
        String name = getString(attributes, "name", email);

        /**
         * OAuth2Attributes 객체 생성
         */
        return new OAuth2Attributes(
                "GOOGLE",
                sub,
                email,
                name
        );
    }

    /**
     * Keycloak OIDC 사용자 정보 변환
     */
    private static OAuth2Attributes ofKeycloak(Map<String, Object> attributes) {

        /**
         * Keycloak 사용자 고유 ID
         *
         * OIDC 표준에서는
         * "sub" 값이 사용자 식별자이다.
         */
        String sub = getRequiredString(attributes, "sub");

        /**
         * 사용자 이메일
         */
        String email = getRequiredString(attributes, "email");

        /**
         * 사용자 이름
         *
         * name 필드가 존재할 경우 사용
         */
        String name = getString(attributes, "name", null);

        /**
         * name 값이 없을 경우
         * preferred_username을 사용한다.
         */
        if (name == null || name.isBlank()) {
            name = getString(attributes, "preferred_username", email);
        }

        /**
         * OAuth2Attributes 객체 생성
         */
        return new OAuth2Attributes(
                "KEYCLOAK",
                sub,
                email,
                name
        );
    }

    /**
     * 필수 attribute 문자열 조회
     *
     * attribute가 없으면 예외 발생
     */
    private static String getRequiredString(Map<String, Object> attributes, String key) {

        Object value = attributes.get(key);

        /**
         * 필수 attribute가 없으면 예외 발생
         */
        if (value == null) {
            throw new IllegalArgumentException("Missing required attribute: " + key);
        }

        return String.valueOf(value);
    }

    /**
     * 선택 attribute 문자열 조회
     *
     * 값이 없으면 defaultValue 반환
     */
    private static String getString(Map<String, Object> attributes, String key, String defaultValue) {

        Object value = attributes.get(key);

        /**
         * 값이 없으면 기본값 반환
         */
        return value == null ? defaultValue : String.valueOf(value);
    }
}