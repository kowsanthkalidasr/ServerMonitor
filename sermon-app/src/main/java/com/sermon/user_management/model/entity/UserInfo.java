package com.sermon.user_management.model.entity;


import com.sermon.config.security.audit.entity.CreateUpdateAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserInfo extends CreateUpdateAuditEntity {

    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id;


    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id", referencedColumnName="id")
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;


    @Column(name = "last_name")
    private String lastName;


    @Column(name = "description")
    private String description;


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
//    private User createdBy;
//
//
//    @Column(name = "created_time", nullable = false,updatable=false)
//    private Instant createdTime;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "updated_by", referencedColumnName = "id", nullable = false)
//    private User updatedBy;
//
//
//    @Column(name = "updated_time", nullable = false)
//    private Instant updatedTime;


//    @PrePersist
//    protected void onCreate(){
//        this.createdTime = Instant.now();
//        this.updatedTime = Instant.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedTime = Instant.now();
//    }

}
