package com.example.demo.security.jwt;

import com.example.demo.auth.service.AccessTokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    /**
     * JWT 인증 제외 대상
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/")
                || path.startsWith("/error")
                || path.startsWith("/auth/refresh"); // refresh 도 제외시켜야 함
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String header = request.getHeader("Authorization");

            /**
             * 토큰 없으면 그냥 통과
             */
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);

            /**
             * 블랙리스트 체크
             */
            if (accessTokenBlacklistService.isBlacklisted(token)) {
                log.debug("Blacklisted token");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            /**
             * 토큰 검증
             */
            if (!jwtProvider.validateToken(token)) {
                log.debug("Invalid token");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            /**
             * 사용자 정보 추출
             */
            Long userId = jwtProvider.getUserId(token);
            List<String> roles = jwtProvider.getRoles(token);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            MDC.put("userId", String.valueOf(userId));

        } catch (ExpiredJwtException e) {

            log.debug("JWT expired: {}", e.getMessage());
            SecurityContextHolder.clearContext();

        } catch (JwtException e) {

            log.warn("Invalid JWT token");
            SecurityContextHolder.clearContext();

        } catch (Exception e) {

            log.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();

        } finally {
            MDC.remove("userId");
        }

        /**
         * 무조건 다음 필터 진행
         */
        filterChain.doFilter(request, response);
    }
}