package com.sermon.user_management.model.entity.id;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserPermissionId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "permission_id")
    private Long permissionId;

}
