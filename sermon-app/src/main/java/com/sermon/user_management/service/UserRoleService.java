package com.sermon.user_management.service;


import com.sermon.user_management.model.entity.User;
import com.sermon.user_management.repository.RoleRepository;
import com.sermon.user_management.repository.UserRepository;
import com.sermon.user_management.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
public class UserRoleService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository UserRoleRepository;


    @Transactional
    public void assignRolesToUser(User user, Set<Long> roleIds) {
        
    }



}
