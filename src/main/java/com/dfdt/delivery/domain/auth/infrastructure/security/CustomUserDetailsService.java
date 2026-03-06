package com.dfdt.delivery.domain.auth.infrastructure.security;

import com.dfdt.delivery.domain.user.domain.entity.User;
import com.dfdt.delivery.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security에서 사용자 정보를 조회하기 위해 사용하는 서비스 클래스.
 * DB의 유저 정보를 조회하여 Security 전용 객체인 CustomUserDetails로 가공하여 반환.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 아이디(username)를 통해 DB에서 유저 정보를 조회.
     * @param username 조회할 사용자 아이디
     * @return 인증에 필요한 사용자 상세 객체 (CustomUserDetails)
     * @throws UsernameNotFoundException 해당 사용자가 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserDetails(user);
    }
}
