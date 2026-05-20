package com.scanner.demo.user.repository;

import com.scanner.demo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // 로그인 시 입력받은 user_id로 DB에서 사용자를 조회하는 메서드
    Optional<User> findByUserId(String userId);
}