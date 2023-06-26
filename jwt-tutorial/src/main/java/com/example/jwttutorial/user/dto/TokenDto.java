package com.example.jwttutorial.user.dto;

import lombok.*;

/**
 * 토큰 정보를 Response할 때 사용할 dto
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {

    private String token;
}