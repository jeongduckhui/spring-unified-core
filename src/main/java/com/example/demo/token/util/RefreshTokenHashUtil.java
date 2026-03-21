package com.example.demo.token.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * RefreshToken 해시 유틸리티
 *
 * RefreshToken 원문을 DB에 저장하지 않고
 * SHA-256 해시값으로 변환하기 위한 클래스이다.
 *
 * 보안 이유
 *
 * 만약 DB가 탈취될 경우
 *
 * refresh_token 테이블에
 * 원문 토큰이 저장되어 있으면
 * 공격자가 바로 로그인 세션을 탈취할 수 있다.
 *
 * 따라서
 *
 * rawRefreshToken → SHA256 hash → DB 저장
 *
 * 방식으로 저장한다.
 *
 * 이후 RefreshToken 검증 시
 *
 * 요청 token → hash → DB 비교
 *
 * 방식으로 검증한다.
 */
@Component
public class RefreshTokenHashUtil {

    /**
     * RefreshToken을 SHA-256 해시로 변환
     *
     * 예
     *
     * rawToken
     * 550e8400-e29b-41d4-a716-446655440000
     *
     * hash
     * a2c3f... (64 hex length)
     */
    public String hash(String rawToken) {

        try {

            /**
             * SHA-256 해시 알고리즘 생성
             */
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            /**
             * 문자열을 UTF-8 바이트 배열로 변환
             */
            byte[] hashed = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            /**
             * byte[] → hex 문자열 변환
             */
            return bytesToHex(hashed);

        } catch (NoSuchAlgorithmException e) {

            /**
             * SHA-256 알고리즘이 없는 경우
             * (실제로는 거의 발생하지 않는다)
             */
            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }

    /**
     * byte 배열을 hex 문자열로 변환
     *
     * 예
     *
     * byte[]
     * → "a3f91c..."
     */
    private String bytesToHex(byte[] bytes) {

        /**
         * hex 문자열 생성용 StringBuilder
         *
         * byte 하나가 hex 2자리이므로
         * 길이는 bytes.length * 2
         */
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        /**
         * byte 배열 순회
         */
        for (byte b : bytes) {

            /**
             * %02x
             *
             * hex 문자열로 변환
             * 항상 2자리 유지
             */
            sb.append(String.format("%02x", b));
        }

        /**
         * 최종 hex 문자열 반환
         */
        return sb.toString();
    }
}