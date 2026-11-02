package com.skyward.api;
import com.skyward.api.ApiDtos.*;
import com.skyward.security.AppPrincipal;
import com.skyward.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/bookings")
public class BookingController {
 private final BookingService bookings;private final JdbcTemplate db;public BookingController(BookingService bookings,JdbcTemplate db){this.bookings=bookings;this.db=db;}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody CreateBooking body,Authentication auth){return bookings.create(principal(auth),body);}
 @PostMapping("/{id}/pay") public Map<String,Object> pay(@PathVariable long id,@RequestBody(required=false)Pay body,Authentication auth){return bookings.pay(id,principal(auth),body==null?"CARD":body.paymentMethod());}
 @GetMapping("/mine") public Object mine(Authentication auth){return bookings.myBookings(principal(auth).id());}
 @GetMapping("/boarding-passes") public Object passes(Authentication auth){return bookings.boardingPasses(principal(auth).id());}
 @PostMapping("/{id}/cancellation-request") public Map<String,Object> requestCancel(@PathVariable long id,Authentication auth){return bookings.requestCancellation(id,principal(auth));}
 @GetMapping("/{id}/refund-summary") public Map<String,Object> refund(@PathVariable long id,Authentication auth){return db.queryForMap("SELECT c.booking_id bookingId,c.flight_number flightNumber,c.departure_city departureCity,c.destination_city destinationCity,c.departure_time departureTime,c.cancelled_at cancelledAt,c.seconds_before_departure secondsBeforeDeparture,c.original_amount originalAmount,c.refund_percentage refundPercentage,c.refunded_amount refundedAmount,c.refund_reason refundReason FROM booking_cancellations c WHERE c.booking_id=? AND c.user_id=?",id,principal(auth).id());}
 private AppPrincipal principal(Authentication auth){return (AppPrincipal)auth.getPrincipal();}
}
