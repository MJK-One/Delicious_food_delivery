package com.dfdt.delivery.domain.user.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    /**
     * 주어진 사용자 이름을 가진 사용자를 조회합니다.
     * @param username username 검색할 사용자의 아이디(username)
     * @return 해당 이름을 가진 사용자를 포함하는 Optional 객체.
     */
    @Override
    Optional<User> findByUsername(String username);

    /**
     * Soft Delete 된 데이터까지 포함하여 아이디 중복 여부를 확인합니다.
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM p_user WHERE username = :username", nativeQuery = true)
    boolean existsByUsernameIncludeDeleted(@Param("username") String username);
}
