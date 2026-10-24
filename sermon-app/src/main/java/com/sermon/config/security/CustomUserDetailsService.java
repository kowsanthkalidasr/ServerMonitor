package com.sermon.config.security;


import com.sermon.user_management.model.entity.User;
import com.sermon.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @NullMarked
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername (String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email).orElseThrow(() -> new UsernameNotFoundException("user with email "+ email + "not found"));
        return new CustomUserDetails(user);
    }
}
