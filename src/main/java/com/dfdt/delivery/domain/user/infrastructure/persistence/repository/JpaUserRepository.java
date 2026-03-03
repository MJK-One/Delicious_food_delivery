package com.dfdt.delivery.domain.user.infrastructure.persistence.repository;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    @Override
    Optional<User> findByUsername(String username);
}
