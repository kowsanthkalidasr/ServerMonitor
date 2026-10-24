package com.sermon.user_management.repository;

import com.sermon.user_management.model.entity.UserRole;
import com.sermon.user_management.model.entity.id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole,UserRoleId> {


}
