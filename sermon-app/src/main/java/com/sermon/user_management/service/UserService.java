package com.sermon.user_management.service;


import com.sermon.exception.ResourceNotFoundException;
import com.sermon.user_management.model.entity.Role;
import com.sermon.user_management.model.entity.User;
import com.sermon.user_management.model.entity.UserInfo;
import com.sermon.user_management.model.entity.UserRole;
import com.sermon.user_management.model.enums.UserStatus;
import com.sermon.user_management.repository.*;
import com.sermon.user_management.web.dto.UserCreationRequest;
import com.sermon.user_management.model.mapper.UserInfoMapper;
import com.sermon.user_management.model.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final UserInfoRepository userInfoRepository;

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserCreationRequest request){
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE.getCode());
        userRepository.save(user);

        for (Long roleId : request.roleIds()) {
            addUserRole(user,roleId);
        }

        UserInfo userInfo = userInfoMapper.toEntity(request);
        userInfo.setUser(user);

        userInfoRepository.save(userInfo);

        return user;
    }

    public void addUserRole(User user, Long roleId) throws ResourceNotFoundException{
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found for roleId : " + roleId));
        userRoleRepository.save(new UserRole(user,role));
    }



    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(UUID userId) {
        return userRepository.findById(userId);
    }


    public boolean userExistsById(UUID userId) {
        return userRepository.existsById(userId);
    }
    public boolean userExistsByUsername(String name) {
        return userRepository.existsByUsername(name);
    }
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
