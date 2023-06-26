package com.example.jwttutorial.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    private SecurityUtil() {
    }

    /**
     * Security Context의 Authentication 객체를 이용해 username을 리턴해주는 간단한 유틸성 메서드
     */
    public static Optional<String> getCurrentUsername() {


        // Security Context에 Authentication 객체가 저장되는 시점은 JwtFilter의 doFilter메서드에서
        // Request가 들어올 때 SecurityContext에 Authentication 객체를 저장해서 사용하게 된다.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        // Optional은 NullPointerException을 방지하고 코드의 안정성을 높이기 위해 사용
        // username이 null이면, Optional.empty()를 반환. 비어있는 Optional 객체 생성
        // username이 null이 아니면, Optional.of(username)를 반환. 해당 값을 감싼 Optional 객체 생성
        return Optional.ofNullable(username);
    }
}
