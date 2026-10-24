package com.sermon.user_management.repository;

import com.sermon.user_management.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role,Long> {

    public List<Role> findAllByIdIn(Set<Long> roleIds);

    public Optional<Role> findById(Long roleId);



}
