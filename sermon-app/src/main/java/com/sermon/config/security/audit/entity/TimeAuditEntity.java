package com.sermon.config.security.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class TimeAuditEntity {

    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private Instant createdTime;

    @LastModifiedDate
    @Column(name = "updated_time", nullable = false)
    private Instant updatedTime;
}