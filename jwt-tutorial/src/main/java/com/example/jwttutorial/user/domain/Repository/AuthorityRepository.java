package com.example.jwttutorial.user.domain.Repository;

import com.example.jwttutorial.user.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
