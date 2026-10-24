package com.sermon.user_management.model.entity;


import com.sermon.config.security.SecurityUtil;
import com.sermon.user_management.model.entity.id.UserRoleId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import com.sermon.config.security.SecurityUtil;

@Entity
@Table(name="users_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false,updatable = false)
    private User assignedBy;


    @Column(name = "assigned_time", nullable = false, updatable = false)
    private Instant assignedAt;


    public UserRole (User user, Role role) {
        this.user = user;
        this.role = role;
        this.assignedBy = SecurityUtil.getCurrentUser();
        this.assignedAt = Instant.now();
        this.id = new UserRoleId(user.getId(), role.getId());
    }
}
