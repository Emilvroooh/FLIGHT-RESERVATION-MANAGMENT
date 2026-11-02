package com.skyward.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="users", uniqueConstraints=@UniqueConstraint(name="uq_users_email",columnNames="email"))
public class UserEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="user_id") private Long id;
    @Column(nullable=false,length=120) private String name;
    @Column(nullable=false,length=254) private String email;
    @Column(length=32) private String phone;
    @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
    @Column(name="created_at",insertable=false,updatable=false) private LocalDateTime createdAt;
    protected UserEntity() {}
    public UserEntity(String name,String email,String phone,String passwordHash){this.name=name;this.email=email;this.phone=phone;this.passwordHash=passwordHash;}
    public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getPhone(){return phone;} public String getPasswordHash(){return passwordHash;} public LocalDateTime getCreatedAt(){return createdAt;}
    public void setPasswordHash(String value){passwordHash=value;}
}
