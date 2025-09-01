package com.example.testmanagement.Entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>();
}
