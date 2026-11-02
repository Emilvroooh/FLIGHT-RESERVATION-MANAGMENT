package com.skyward.repository;
import com.skyward.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface BookingRepository extends JpaRepository<BookingEntity,Long> {
 List<BookingEntity> findByUserIdOrderByBookingDateDesc(Long userId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from BookingEntity b where b.id=:id") Optional<BookingEntity> lockById(@Param("id")Long id);
}
