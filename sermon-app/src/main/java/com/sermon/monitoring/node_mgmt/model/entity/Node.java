package com.sermon.monitoring.node_mgmt.model.entity;


import com.sermon.config.security.audit.entity.CreateUpdateAuditEntity;
import com.sermon.monitoring.node_mgmt.model.enums.NodeOpStatus;
import com.sermon.monitoring.node_mgmt.model.enums.NodeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;


@Entity
@Table(name = "nodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString


@SQLDelete(sql = "UPDATE nodes SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted= false")

public class Node extends CreateUpdateAuditEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UniqueIdentifier")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "os_name", nullable = false, length = 100)
    private String osName;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NodeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "op_status", nullable = false, length = 20)
    private NodeOpStatus opStatus;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;


    @PrePersist
    public void prePersist() {
        if (deleted == null) {
            deleted = false;
        }
    }
}
