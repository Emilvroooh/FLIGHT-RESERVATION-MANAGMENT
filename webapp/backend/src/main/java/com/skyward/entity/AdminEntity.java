package com.skyward.entity;
import jakarta.persistence.*;
@Entity @Table(name="admins",uniqueConstraints=@UniqueConstraint(name="uq_admins_username",columnNames="username"))
public class AdminEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="admin_id") private Long id;
 @Column(nullable=false,length=80) private String username;
 @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
 protected AdminEntity(){} public AdminEntity(String username,String hash){this.username=username;this.passwordHash=hash;}
 public Long getId(){return id;}public String getUsername(){return username;}public String getPasswordHash(){return passwordHash;}public void setPasswordHash(String value){passwordHash=value;}
}
