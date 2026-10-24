package com.sermon.config.security;


import com.sermon.user_management.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtil {

    private SecurityUtil(){
        throw new UnsupportedOperationException("SecurityUtil is a utility class and not meant to be extended or instantiated");
    }


    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new IllegalStateException("No Authenticated Session Found");
        }
        CustomUserDetails principal = (CustomUserDetails)authentication.getPrincipal();
        return principal.getUser();
    }
}
