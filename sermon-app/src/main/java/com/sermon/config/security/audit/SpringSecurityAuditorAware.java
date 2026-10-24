package com.sermon.config.security.audit;

import com.sermon.config.security.CustomUserDetails;
import com.sermon.user_management.model.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.attribute.UserPrincipal;
import java.util.Optional;
import java.util.UUID;

public class SpringSecurityAuditorAware implements AuditorAware<User> {
    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            return Optional.ofNullable(principal.getUser());
    }
}
