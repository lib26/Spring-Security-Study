package com.example.jwttutorial.user.application;

import com.example.jwttutorial.user.domain.Authority;
import com.example.jwttutorial.user.domain.Repository.UserRepository;
import com.example.jwttutorial.user.domain.User;
import com.example.jwttutorial.user.dto.UserDto;
import com.example.jwttutorial.user.exception.DuplicateMemberException;
import com.example.jwttutorial.user.exception.NotFoundMemberException;
import com.example.jwttutorial.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 회원가입, 유저정보조회 등의 메소드를 만들기 위한 클래스
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * signup 메서드를 통해 가입한 회원은 USER ROLE을 가지고 있다.
     * data.sql에서 자동 생성되는 admin 계정은 USER, ADMIN ROLE을 가지고 있다.
     */
    @Transactional
    public UserDto signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority)) // 단일 요소로 가지는 불변(Set) 컬렉션을 생성
                .activated(true)
                .build();

        return UserDto.from(userRepository.save(user));
    }

    /**
     * 해당 username을 기준으로 user 조회를 한다.
     * admin만 메서드 실행이 가능하다.
     */
    @Transactional(readOnly = true)
    public UserDto getUserWithAuthorities(String username) {
        return UserDto.from(userRepository.findOneWithAuthoritiesByUsername(username).orElse(null));
        // 존재하지 않는 예외 터져야하지 않나?
    }

    /**
     * SecurityContext에 저장된 username의 정보만 가져온다.
     * 말 그대로 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserDto getMyUserWithAuthorities() {
        return UserDto.from(
                SecurityUtil.getCurrentUsername()
                        .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                        .orElseThrow(() -> new NotFoundMemberException("Member not found"))
        );
    }
}
