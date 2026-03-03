package com.dfdt.delivery.domain.user.domain.repository;

import com.dfdt.delivery.domain.user.domain.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
}
