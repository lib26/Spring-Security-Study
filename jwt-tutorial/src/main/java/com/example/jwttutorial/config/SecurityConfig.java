package com.example.jwttutorial.config;

import com.example.jwttutorial.security.jwt.JwtAccessDeniedHandler;
import com.example.jwttutorial.security.jwt.JwtAuthenticationEntryPoint;
import com.example.jwttutorial.security.jwt.JwtSecurityConfig;
import com.example.jwttutorial.security.jwt.TokenProvider;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
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
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            TokenProvider tokenProvider,
            CorsFilter corsFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .anyRequest().authenticated()
                )

                // 세션을 사용하지 않기 때문에 STATELESS로 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/api/hello").permitAll()
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }

}
