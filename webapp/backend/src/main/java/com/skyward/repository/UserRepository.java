package com.skyward.repository;
import com.skyward.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<UserEntity,Long> {
 Optional<UserEntity> findByEmailIgnoreCase(String email);
 boolean existsByEmailIgnoreCase(String email);
 List<UserEntity> findTop50ByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(String name,String email);
}
