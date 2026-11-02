package com.skyward.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="password_reset_tokens",uniqueConstraints=@UniqueConstraint(name="uq_password_reset_token_hash",columnNames="token_hash"))
public class PasswordResetTokenEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="reset_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @Column(name="token_hash",nullable=false,length=100) private String tokenHash;
 @Column(name="expires_at",nullable=false) private LocalDateTime expiresAt;
 @Column(name="used_at") private LocalDateTime usedAt;
 protected PasswordResetTokenEntity(){}public PasswordResetTokenEntity(UserEntity user,String tokenHash,LocalDateTime expiresAt){this.user=user;this.tokenHash=tokenHash;this.expiresAt=expiresAt;}public Long getId(){return id;}public UserEntity getUser(){return user;}public String getTokenHash(){return tokenHash;}public LocalDateTime getExpiresAt(){return expiresAt;}public LocalDateTime getUsedAt(){return usedAt;}public void markUsed(LocalDateTime at){usedAt=at;}
}
