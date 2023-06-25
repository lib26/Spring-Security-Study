package com.example.jwttutorial.jwt;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 5. 필요한 권한이 존재하지 않는 경우 403 Forbidden 에러를 리턴하는 클래스
 * 403 Forbidden 에러란? 인증은 되었지만 해당 리소스에 접근할 수 있는 권한이 없다는 의미.
 * ex) 로그인은 되었지만 다른 유저의 구매목록을 조회(접근)하려고 할 때 403이 발생한다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}