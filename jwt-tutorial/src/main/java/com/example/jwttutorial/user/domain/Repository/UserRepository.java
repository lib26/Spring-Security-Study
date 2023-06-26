package com.example.jwttutorial.user.domain.Repository;

import com.example.jwttutorial.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * username을 기준으로 User 정보를 가져올 때 권한 정보도 같이 가져온다.
     * @EntityGraph : 쿼리가 수행될 때 Lazy 조회가 아닌 Eager 조회로
     * User 엔티티의 "authorities" 속성을 Eager 로딩하도록 설정.
     * 즉, 사용자를 조회할 때 연관된 권한(authorities) 역시 함께 로드되어 영속성 컨텍스트에 캐시되어 성능상의 이점을 가져올 수 있다.
     */
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByUsername(String username);
}
