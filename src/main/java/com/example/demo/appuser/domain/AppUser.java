package com.example.demo.appuser.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor
public class AppUser {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    public AppUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}