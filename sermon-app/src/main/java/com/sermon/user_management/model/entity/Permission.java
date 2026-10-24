package com.sermon.user_management.model.entity;


import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name",unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;
}
