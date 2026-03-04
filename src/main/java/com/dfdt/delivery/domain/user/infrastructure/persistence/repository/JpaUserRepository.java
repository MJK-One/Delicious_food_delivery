package com.dfdt.delivery.domain.user.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    /**
     * 주어진 사용자 이름을 가진 사용자를 조회합니다.
     * @param username username 검색할 사용자의 아이디(username)
     * @return 해당 이름을 가진 사용자를 포함하는 Optional 객체.
     */
    @Override
    Optional<User> findByUsername(String username);
}