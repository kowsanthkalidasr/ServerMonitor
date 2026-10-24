package com.sermon.user_management.model.entity;


import com.sermon.user_management.model.entity.id.UserPermissionId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users_permissions")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {
    @EmbeddedId
    private UserPermissionId id = new UserPermissionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", referencedColumnName = "id")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false, referencedColumnName = "id")
    private User assignedBy;


    @Column(name = "assigned_time", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    public UserPermission (User user, Permission permission,User assignedBy) {
        this.user = user;
        this.permission = permission;
        this.assignedBy = assignedBy;
        this.id = new UserPermissionId(user.getId(), permission.getId());
    }
}
