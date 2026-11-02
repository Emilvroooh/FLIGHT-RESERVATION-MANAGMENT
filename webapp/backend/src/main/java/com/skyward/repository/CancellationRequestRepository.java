package com.skyward.repository;
import com.skyward.entity.CancellationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface CancellationRequestRepository extends JpaRepository<CancellationRequestEntity,Long> {
 List<CancellationRequestEntity> findByBookingIdAndRequestStatus(Long bookingId,String requestStatus);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from CancellationRequestEntity r where r.id=:id") Optional<CancellationRequestEntity> lockById(@Param("id")Long id);
}
