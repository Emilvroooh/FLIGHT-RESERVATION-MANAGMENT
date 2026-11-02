package com.skyward.repository;
import com.skyward.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PassengerRepository extends JpaRepository<PassengerEntity,Long> { List<PassengerEntity> findByBookingIdOrderByIdAsc(Long bookingId); }
