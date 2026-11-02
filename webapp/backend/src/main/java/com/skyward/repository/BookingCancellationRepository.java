package com.skyward.repository;
import com.skyward.entity.BookingCancellationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface BookingCancellationRepository extends JpaRepository<BookingCancellationEntity,Long> { Optional<BookingCancellationEntity> findByBookingIdAndUserId(Long bookingId,Long userId); }
