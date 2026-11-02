package com.skyward.repository;
import com.skyward.entity.FlightEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface FlightRepository extends JpaRepository<FlightEntity,Long> {
 @EntityGraph(attributePaths="airline")
 @Query("select f from FlightEntity f where f.departureTime >= :now and (:fromCity is null or lower(f.departureCity) like lower(concat('%',:fromCity,'%'))) and (:toCity is null or lower(f.destinationCity) like lower(concat('%',:toCity,'%'))) and (:dayStart is null or (f.departureTime >= :dayStart and f.departureTime < :dayEnd)) and (:maxPrice is null or f.economyPrice <= :maxPrice) order by f.departureTime")
 List<FlightEntity> search(@Param("fromCity")String fromCity,@Param("toCity")String toCity,@Param("dayStart")LocalDateTime dayStart,@Param("dayEnd")LocalDateTime dayEnd,@Param("maxPrice")BigDecimal maxPrice,@Param("now")LocalDateTime now);
 @EntityGraph(attributePaths="airline") List<FlightEntity> findAllByOrderByDepartureTimeAsc();
 @EntityGraph(attributePaths="airline") Optional<FlightEntity> findWithAirlineById(Long id);
}
