package com.example.jwttutorial.security.jwt;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 4. 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러를 제공하는 클래스
 * 401 Unauthorized란? 인증이 되어있지 않다는 의미
 * ex) 회원(로그인을 한 유저)만 접근할 수 있는 페이지에 접근하려고 할 때 401이 발생한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
