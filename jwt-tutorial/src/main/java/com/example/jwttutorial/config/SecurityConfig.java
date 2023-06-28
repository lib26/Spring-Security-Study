package com.example.jwttutorial.config;

import com.example.jwttutorial.security.jwt.JwtAccessDeniedHandler;
import com.example.jwttutorial.security.jwt.JwtAuthenticationEntryPoint;
import com.example.jwttutorial.security.jwt.JwtSecurityConfig;
import com.example.jwttutorial.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * 두 애너테이션은 스프링 시큐리티와 관련된 설정을 활성화하는 역할
 *
 * @EnableWebSecurity WebSecurityConfigurerAdapter 클래스를 확장한 구성 클래스를 생성하여 웹 보안 구성을 할 수 있다.
 * @EnableMethodSecurity 스프링 시큐리티의 메소드 수준 보안 설정을 활성화한다.
 * + @PreAuthorize, @PostAuthorize, @Secured 등과 같은 애너테이션을 사용하여 메소드의 호출에 대한 보안 규칙을 정의할 수 있다.
 * + 메소드 수준 보안은 각 메소드가 호출될 때 사용자의 인증 정보와 권한을 기반으로 해당 메소드가 실행될지 여부를 결정하는 데 사용됨.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 스프링 시큐리티는 '서블릿 필터' 기반으로 동작하면서 스프링의 많은 지원을 '함께' 사용할 수 있도록 했다.
     * 서블릿과 스프링의 컨텍스트는 다르다. 서블릿은 톰캣과 같은 WAS 단에서 동작하며 모든 웹 요청을 먼저 처리한다.
     * 앞선 필터의 과정을 모두 거치고 난 다음에야 요청은 스프링 컨텍스트로 넘어온다.
     * 다시 말하면, 필터에서는 스프링의 기능을 사용할 수 없다는 말이다.
     * 그렇다면 필터에서도 스프링 기능을 사용하는 방법을 어떻게 만들어냈을까?
     *
     * 스프링 시큐리티 프레임워크의 동작 원리와 SecurityFilterChain의 역할
     * Client의 요청 -> 여러 서블릿 필터를 거치면서 그 중 DelegatingFilterProxy에 도달 ->
     * -> FilterChainProxy(Spring Bean 객체)에게 요청 처리를 '위임' -> SecurityFilterChain ->
     * -> SecurityFilterChain 객체에는 SecurityFilterChain 타입의 List가 있음 ->
     * -> 하나의 SecurityFilterChain 구현체는 List<Filter>를 갖고 있음
     * -> 즉, 요청에 맞는 SecurityFilterChain 구현체가 FilterCHainProxy(Spring Bean 객체)에게 자신의 필터 체인을 제공함.
     * 이렇게 모든 필터를 순회하면서 인증 및 인가 처리를 한다.
     * 그리고 마지막 필터까지 예외가 발생하지 않으면 나머지 필터를 건너다 결국 스프링의 DispatcherServlet으로 넘어가고 비즈니스 로직을 만나게 된다.
     * 참고자료(매우 유익..) : https://somuchthings.tistory.com/195
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // token을 사용하는 방식이기 때문에 csrf를 disable합니다.
                .csrf(csrf -> csrf.disable())

                // 다른 도메인에서의 요청을 허용
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler) // 403 예외처리 헨들러 설정
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 에외처리 헨들러 설정
                )

                // 토큰 없이 접근을 허용할 api 설정
                // 홈 화면, 토큰을 받기위한 api, 회원가입을 위한 api
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/api/hello", "/api/authenticate", "/api/signup").permitAll()
                        .anyRequest().authenticated()
                )

                // 세션을 사용하지 않기 때문에 STATELESS로 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

}
