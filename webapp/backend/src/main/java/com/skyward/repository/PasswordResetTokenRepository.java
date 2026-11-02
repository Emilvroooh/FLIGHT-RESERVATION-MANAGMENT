package com.skyward.repository;
import com.skyward.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select t from PasswordResetTokenEntity t where t.tokenHash=:hash and t.usedAt is null and t.expiresAt>:now")
 Optional<PasswordResetTokenEntity> lockValidToken(@Param("hash")String hash,@Param("now")LocalDateTime now);
}
