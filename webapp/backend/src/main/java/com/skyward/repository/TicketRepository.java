package com.skyward.repository;
import com.skyward.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface TicketRepository extends JpaRepository<TicketEntity,Long> {
 @Query("select distinct upper(trim(t.seatNumber)) from TicketEntity t where t.flight.id=:flightId and t.ticketStatus='ISSUED' and t.booking.status in ('PENDING','CONFIRMED')")
 List<String> findActiveSeats(@Param("flightId")Long flightId);
 @Query("select t.cabinClass,count(distinct upper(trim(t.seatNumber))) from TicketEntity t where t.flight.id=:flightId and t.ticketStatus='ISSUED' and t.booking.status in ('PENDING','CONFIRMED') group by t.cabinClass")
 List<Object[]> countActiveSeatsByCabin(@Param("flightId")Long flightId);
}
