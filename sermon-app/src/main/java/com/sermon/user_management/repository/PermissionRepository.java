package com.sermon.user_management.repository;

import com.sermon.user_management.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission,Long> {

    public List<Permission> findAllByIdIn(Set<Long> permissionIds);

}
