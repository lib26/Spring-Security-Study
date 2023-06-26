package com.example.jwttutorial.user.presentation;

import com.example.jwttutorial.security.jwt.JwtFilter;
import com.example.jwttutorial.security.jwt.TokenProvider;
import com.example.jwttutorial.user.dto.LoginDto;
import com.example.jwttutorial.user.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    /**
     * 로그인 API
     * 약간 어려운 내용. 내부 로직을 이해해야할 필요.
     * https://www.inflearn.com/course/lecture?courseSlug=%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-jwt&unitId=65762&tab=community&q=349502&category=questionDetail
     * https://lilly021.com/spring-security-architecture
     */
    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

        // 1. UsernamePasswordAuthenticationToken 객체를 만들고
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // 2. 적절한 authentication provider를 선택하기 위해 authenticationManager에게 위의 객체를 전달한다.
        // 3. AuthenticationManager의 구현체인 ProviderManager의 authenticate() 메소드가 실행된다.
        // 4. 해당 메소드에선 적절한 AuthenticaionProvider 고른 뒤(무슨 기준으로 찾는지는 너무 디테일해보인다..) authenticate() 메소드를 실행한다.
        // 5. 각각의 전략을 갖는 authentication provider들은 시스템으로부터 user 정보를 받아야한다. 각각의 provider들은 user를 얻는 방식이 다를뿐이다.
        // 6. 내부 로직을 따라가보니 DaoAuthenticationProvider에서 loadUserByUsername가 구현되어있는 것을 확인 가능.
        // 결국 아래코드 상에서 authenticate 메서드가 실행이 될 때 CustomUserDetailsService의 loadUserByUsername 메서드가 실행되어 Dao를 통해 user정보를 얻게된다.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증에 성공하면(로그인에서 입력했던 id에 해당하는 DB에 올바른 user가 있다면) filter는 해당 Authentication를 SecurityContext에 set한다.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);

        // Response Header에도 JWT 토큰을 넣어주고,
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        // Response Body에도 JWT 토큰을 넣어서 리턴해준다.
        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
}
