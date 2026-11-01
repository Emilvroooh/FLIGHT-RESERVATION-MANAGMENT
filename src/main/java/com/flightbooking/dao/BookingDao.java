package com.flightbooking.dao;

import com.flightbooking.config.DatabaseConnection;
import com.flightbooking.model.BookingSummary;
import com.flightbooking.model.CancellationRequest;
import com.flightbooking.model.BoardingPass;
import com.flightbooking.model.PassengerInput;
import com.flightbooking.model.TicketInfo;
import com.flightbooking.model.RefundSummary;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BookingDao {
    private static final String SUMMARY="SELECT b.booking_id,b.status,u.name,u.email,f.flight_number,f.departure_city,f.destination_city,b.total_amount,b.booking_date,f.departure_time,(SELECT cr.request_status FROM cancellation_requests cr WHERE cr.booking_id=b.booking_id ORDER BY cr.request_id DESC LIMIT 1) FROM bookings b JOIN users u ON u.user_id=b.user_id JOIN flights f ON f.flight_id=b.flight_id ";
    public long create(long userId,long flightId,List<PassengerInput> passengers) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                BigDecimal economyPrice,businessPrice;int economyCapacity,businessCapacity;
                try(PreparedStatement p=c.prepareStatement("SELECT economy_ticket_price,business_ticket_price,economy_capacity,business_capacity,departure_time FROM flights WHERE flight_id=? FOR UPDATE")){p.setLong(1,flightId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Flight was not found.");economyPrice=r.getBigDecimal(1);businessPrice=r.getBigDecimal(2);economyCapacity=r.getInt(3);businessCapacity=r.getInt(4);if(r.getTimestamp(5).toLocalDateTime().isBefore(java.time.LocalDateTime.now()))throw new SQLException("This flight has already departed.");}}
                int[] occupiedByClass=activeCabinCounts(c,flightId);int economyAvailable=Math.max(0,economyCapacity-occupiedByClass[0]),businessAvailable=Math.max(0,businessCapacity-occupiedByClass[1]);
                int requestedEconomy=0,requestedBusiness=0;
                Set<String> requestedSeats=new HashSet<>();
                for(PassengerInput passenger:passengers){String seat=passenger.getSeatNumber()==null?null:passenger.getSeatNumber().trim().toUpperCase(Locale.ROOT),cabin=passenger.getCabinClass();if(seat==null||!requestedSeats.add(seat))throw new SQLException("Choose a unique seat for every passenger.");if(!seat.matches("[1-9][0-9]{0,2}[A-F]"))throw new SQLException("Choose a valid seat number.");if(!"ECONOMY".equals(cabin)&&!"BUSINESS".equals(cabin))throw new SQLException("Choose Economy or Business Class for every passenger.");int ordinal=seatOrdinal(seat);if(ordinal<1||ordinal>businessCapacity+economyCapacity)throw new SQLException("Seat "+seat+" is outside this flight's cabin capacity.");boolean businessSeat=ordinal<=businessCapacity;if(businessSeat!="BUSINESS".equals(cabin))throw new SQLException("Seat "+seat+" does not belong to the selected cabin class.");if(seatTaken(c,flightId,seat))throw new SQLException("Seat "+seat+" is already taken, please choose another.");if("BUSINESS".equals(cabin))requestedBusiness++;else requestedEconomy++;}
                if(requestedEconomy>economyAvailable||requestedBusiness>businessAvailable)throw new SQLException("There are not enough available seats in the selected cabin class.");
                int requestedTotal=passengers.size();
                int newEconomyAvailable=economyAvailable-requestedEconomy,newBusinessAvailable=businessAvailable-requestedBusiness;
                try(PreparedStatement p=c.prepareStatement("UPDATE flights SET economy_available_seats=?,business_available_seats=?,available_seats=? WHERE flight_id=?")){p.setInt(1,newEconomyAvailable);p.setInt(2,newBusinessAvailable);p.setInt(3,newEconomyAvailable+newBusinessAvailable);p.setLong(4,flightId);if(p.executeUpdate()!=1)throw new SQLException("Flight inventory could not be updated; please try again.");}
                BigDecimal total=BigDecimal.ZERO;for(PassengerInput passenger:passengers)total=total.add("BUSINESS".equals(passenger.getCabinClass())?businessPrice:economyPrice);long bookingId;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO bookings(user_id,flight_id,status,total_amount) VALUES(?,?,'PENDING',?)",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,userId);p.setLong(2,flightId);p.setBigDecimal(3,total);p.executeUpdate();try(ResultSet keys=p.getGeneratedKeys()){keys.next();bookingId=keys.getLong(1);}}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO passengers(booking_id,name,age,gender,passport_no) VALUES(?,?,?,?,?)")){for(PassengerInput passenger:passengers){p.setLong(1,bookingId);p.setString(2,passenger.getName());p.setInt(3,passenger.getAge());p.setString(4,passenger.getGender());p.setString(5,passenger.getPassportNo());p.addBatch();}p.executeBatch();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO payments(booking_id,amount,payment_method,payment_status) VALUES(?,?,'CARD','PENDING')")){p.setLong(1,bookingId);p.setBigDecimal(2,total);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("SELECT passenger_id,name FROM passengers WHERE booking_id=? ORDER BY passenger_id")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){int i=0;while(r.next()){PassengerInput passenger=passengers.get(i++);String ticket="TKT-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);BigDecimal fare="BUSINESS".equals(passenger.getCabinClass())?businessPrice:economyPrice;try(PreparedStatement insert=c.prepareStatement("INSERT INTO tickets(ticket_number,booking_id,flight_id,passenger_id,seat_number,cabin_class,ticket_price,ticket_status) VALUES(?,?,?,?,?,?,?,'ISSUED')")){insert.setString(1,ticket);insert.setLong(2,bookingId);insert.setLong(3,flightId);insert.setLong(4,r.getLong(1));insert.setString(5,passenger.getSeatNumber());insert.setString(6,passenger.getCabinClass());insert.setBigDecimal(7,fare);insert.executeUpdate();}}}}
                c.commit();return bookingId;
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;throw new SQLException("Booking could not be saved.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public List<BookingSummary> forUser(long userId) throws SQLException {return summaries(SUMMARY+"WHERE b.user_id=? ORDER BY b.booking_date DESC",userId);}
    public List<BookingSummary> all() throws SQLException {return summaries(SUMMARY+"ORDER BY b.booking_date DESC");}
    public void deleteBookingForAdmin(long bookingId) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                long flightId;String status,paymentStatus;
                try(PreparedStatement p=c.prepareStatement("SELECT b.flight_id,b.status,COALESCE(pm.payment_status,'PENDING') FROM bookings b LEFT JOIN payments pm ON pm.booking_id=b.booking_id WHERE b.booking_id=? FOR UPDATE")){
                    p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Booking was not found.");flightId=r.getLong(1);status=r.getString(2);paymentStatus=r.getString(3);}
                }
                if("CONFIRMED".equals(status))throw new SQLException("Approve a cancellation first. This applies the time-based refund before the booking can be deleted.");
                if("PENDING".equals(status)&&!"PENDING".equals(paymentStatus))throw new SQLException("This booking has a payment record; review/cancel it before deletion.");
                try(PreparedStatement p=c.prepareStatement("SELECT flight_id FROM flights WHERE flight_id=? FOR UPDATE")){p.setLong(1,flightId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Flight was not found.");}}
                if("PENDING".equals(status)){
                    int[] cabinCounts=bookingCabinCounts(c,bookingId);int total=cabinCounts[0]+cabinCounts[1];
                    try(PreparedStatement p=c.prepareStatement("UPDATE flights SET economy_available_seats=economy_available_seats+?,business_available_seats=business_available_seats+?,available_seats=available_seats+? WHERE flight_id=?")){p.setInt(1,cabinCounts[0]);p.setInt(2,cabinCounts[1]);p.setInt(3,total);p.setLong(4,flightId);p.executeUpdate();}
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM bookings WHERE booking_id=?")){p.setLong(1,bookingId);if(p.executeUpdate()!=1)throw new SQLException("Booking was not deleted.");}
                c.commit();
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;throw new SQLException("Booking could not be deleted.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public Set<String> occupiedSeats(long flightId) throws SQLException {
        Set<String> out=new HashSet<>();
        String sql="SELECT t.seat_number FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id JOIN flights f ON f.flight_id=t.flight_id WHERE f.flight_id=? AND t.flight_id=f.flight_id AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED'";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,flightId);try(ResultSet r=p.executeQuery()){while(r.next()){String seat=r.getString(1);if(seat!=null){seat=seat.trim().toUpperCase(Locale.ROOT);if(seat.matches("[1-9][0-9]{0,2}[A-F]"))out.add(seat);}}}
        }
        return out;
    }
    public List<BoardingPass> boardingPassesForUser(long userId) throws SQLException {
        String sql="SELECT t.ticket_number,p.name,p.age,p.gender,p.passport_no,f.flight_number,a.airline_name,f.destination_city,f.departure_time,f.arrival_time,t.seat_number,t.cabin_class,t.ticket_price,b.booking_id FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id JOIN passengers p ON p.passenger_id=t.passenger_id JOIN flights f ON f.flight_id=t.flight_id JOIN airlines a ON a.airline_id=f.airline_id WHERE b.user_id=? AND b.status='CONFIRMED' AND t.ticket_status='ISSUED' ORDER BY f.departure_time,t.seat_number";
        List<BoardingPass> out=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new BoardingPass(r.getString(1),r.getString(2),r.getInt(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8),r.getTimestamp(9).toLocalDateTime(),r.getTimestamp(10).toLocalDateTime(),r.getString(11),r.getString(12),r.getBigDecimal(13),r.getLong(14)));}
        }
        return out;
    }
    public RefundSummary cancellationRefundForUser(long bookingId,long userId) throws SQLException {
        String sql="SELECT booking_id,flight_number,departure_city,destination_city,departure_time,cancelled_at,seconds_before_departure,original_amount,refund_percentage,refunded_amount,refund_reason FROM booking_cancellations WHERE booking_id=? AND user_id=?";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,bookingId);p.setLong(2,userId);try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new SQLException("No approved cancellation refund exists for this booking yet.");
                return new RefundSummary(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getTimestamp(5).toLocalDateTime(),r.getTimestamp(6).toLocalDateTime(),r.getLong(7),r.getBigDecimal(8),r.getBigDecimal(9),r.getBigDecimal(10),r.getString(11));
            }
        }
    }
    private RefundSummary cancelBookingWithinTransaction(Connection c,long bookingId,long userId) throws SQLException {
        long ownerId,flightId;String status,paymentStatus,flightNumber,from,to;BigDecimal original;LocalDateTime departure;
        String sql="SELECT b.user_id,b.flight_id,b.status,b.total_amount,f.flight_number,f.departure_city,f.destination_city,f.departure_time,COALESCE(p.payment_status,'PENDING') FROM bookings b JOIN flights f ON f.flight_id=b.flight_id LEFT JOIN payments p ON p.booking_id=b.booking_id WHERE b.booking_id=? FOR UPDATE";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Booking was not found.");ownerId=r.getLong(1);flightId=r.getLong(2);status=r.getString(3);original=r.getBigDecimal(4);flightNumber=r.getString(5);from=r.getString(6);to=r.getString(7);departure=r.getTimestamp(8).toLocalDateTime();paymentStatus=r.getString(9);}}
        if(ownerId!=userId)throw new SQLException("This booking does not belong to the signed-in passenger.");
        if(!"PENDING".equals(status)&&!"CONFIRMED".equals(status))throw new SQLException("Only active bookings can be cancelled.");
        try(PreparedStatement p=c.prepareStatement("SELECT flight_id FROM flights WHERE flight_id=? FOR UPDATE")){p.setLong(1,flightId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Flight was not found.");}}
        LocalDateTime now=LocalDateTime.now();long seconds=java.time.Duration.between(now,departure).getSeconds();
        if(seconds<=0)throw new SQLException("This flight has already departed; the booking cannot be cancelled.");
        BigDecimal percent;String reason;
        if(!"PAID".equals(paymentStatus)){percent=BigDecimal.ZERO;reason="The booking was not paid, so no payment was collected.";}
        else if(seconds>=48L*3600){percent=new BigDecimal("100.00");reason="Cancelled 48 hours or more before departure: full refund.";}
        else if(seconds>=12L*3600){percent=new BigDecimal("50.00");reason="Cancelled between 12 and 48 hours before departure: 50% refund.";}
        else if(seconds>=2L*3600){percent=new BigDecimal("30.00");reason="Cancelled between 2 and 12 hours before departure: 30% refund.";}
        else{percent=new BigDecimal("20.00");reason="Cancelled less than 2 hours before departure: 20% refund.";}
        BigDecimal refunded=original.multiply(percent).divide(new BigDecimal("100"),2,java.math.RoundingMode.HALF_UP);
        int[] cabinCounts=bookingCabinCounts(c,bookingId);int total=cabinCounts[0]+cabinCounts[1];
        try(PreparedStatement p=c.prepareStatement("UPDATE flights SET economy_available_seats=economy_available_seats+?,business_available_seats=business_available_seats+?,available_seats=available_seats+? WHERE flight_id=?")){p.setInt(1,cabinCounts[0]);p.setInt(2,cabinCounts[1]);p.setInt(3,total);p.setLong(4,flightId);p.executeUpdate();}
        try(PreparedStatement p=c.prepareStatement("UPDATE bookings SET status='CANCELLED' WHERE booking_id=?")){p.setLong(1,bookingId);p.executeUpdate();}
        try(PreparedStatement p=c.prepareStatement("UPDATE tickets SET ticket_status='CANCELLED',cancelled_at=? WHERE booking_id=? AND ticket_status IN ('ISSUED','USED')")){p.setTimestamp(1,Timestamp.valueOf(now));p.setLong(2,bookingId);p.executeUpdate();}
        try(PreparedStatement p=c.prepareStatement("UPDATE cancellation_requests SET request_status='APPROVED',admin_response='Booking cancelled; time-based refund recorded.',reviewed_at=? WHERE booking_id=? AND request_status='PENDING'")){p.setTimestamp(1,Timestamp.valueOf(now));p.setLong(2,bookingId);p.executeUpdate();}
        String newPaymentStatus="PENDING".equals(paymentStatus)?"FAILED":percent.compareTo(new BigDecimal("100"))==0?"REFUNDED":percent.signum()>0?"PARTIALLY_REFUNDED":"PAID";
        try(PreparedStatement p=c.prepareStatement("UPDATE payments SET payment_status=?,refund_amount=?,refund_date=? WHERE booking_id=?")){p.setString(1,newPaymentStatus);p.setBigDecimal(2,refunded);if(refunded.signum()>0)p.setTimestamp(3,Timestamp.valueOf(now));else p.setNull(3,Types.TIMESTAMP);p.setLong(4,bookingId);p.executeUpdate();}
        String insert="INSERT INTO booking_cancellations(booking_id,user_id,flight_id,flight_number,departure_city,destination_city,departure_time,cancelled_at,seconds_before_departure,original_amount,refund_percentage,refunded_amount,refund_reason) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement p=c.prepareStatement(insert)){p.setLong(1,bookingId);p.setLong(2,userId);p.setLong(3,flightId);p.setString(4,flightNumber);p.setString(5,from);p.setString(6,to);p.setTimestamp(7,Timestamp.valueOf(departure));p.setTimestamp(8,Timestamp.valueOf(now));p.setLong(9,seconds);p.setBigDecimal(10,original);p.setBigDecimal(11,percent);p.setBigDecimal(12,refunded);p.setString(13,reason);p.executeUpdate();}
        return new RefundSummary(bookingId,flightNumber,from,to,departure,now,seconds,original,percent,refunded,reason);
    }
    public void requestCancellation(long bookingId,long userId) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                String status;Timestamp departure;
                try(PreparedStatement p=c.prepareStatement("SELECT b.status,f.departure_time FROM bookings b JOIN flights f ON f.flight_id=b.flight_id WHERE b.booking_id=? AND b.user_id=? FOR UPDATE")){p.setLong(1,bookingId);p.setLong(2,userId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Booking was not found for this account.");status=r.getString(1);departure=r.getTimestamp(2);}}
                if(!"PENDING".equals(status)&&!"CONFIRMED".equals(status))throw new SQLException("Only pending or confirmed bookings can be submitted for cancellation.");
                if(departure.toLocalDateTime().isBefore(java.time.LocalDateTime.now()))throw new SQLException("This flight has already departed.");
                try(PreparedStatement p=c.prepareStatement("SELECT request_id FROM cancellation_requests WHERE booking_id=? AND request_status='PENDING' FOR UPDATE")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(r.next())throw new SQLException("A cancellation request is already waiting for admin review.");}}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO cancellation_requests(booking_id,user_id) VALUES(?,?)")){p.setLong(1,bookingId);p.setLong(2,userId);p.executeUpdate();}
                c.commit();
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;throw new SQLException("Cancellation request could not be saved.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public List<CancellationRequest> cancellationRequests() throws SQLException {
        String sql="SELECT cr.request_id,cr.booking_id,u.name,u.email,b.status,f.flight_number,f.departure_city,f.destination_city,cr.request_status,cr.request_date,f.departure_time FROM cancellation_requests cr JOIN bookings b ON b.booking_id=cr.booking_id JOIN users u ON u.user_id=cr.user_id JOIN flights f ON f.flight_id=b.flight_id ORDER BY CASE cr.request_status WHEN 'PENDING' THEN 0 ELSE 1 END,cr.request_date DESC";
        List<CancellationRequest> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new CancellationRequest(r.getLong(1),r.getLong(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8),r.getString(9),r.getTimestamp(10).toLocalDateTime(),r.getTimestamp(11).toLocalDateTime()));}return out;
    }
    public void resolveCancellationRequest(long requestId,long adminId,boolean approve) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                long bookingId,userId;
                try(PreparedStatement p=c.prepareStatement("SELECT booking_id,user_id FROM cancellation_requests WHERE request_id=?")){p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Cancellation request was not found.");bookingId=r.getLong(1);userId=r.getLong(2);}}
                try(PreparedStatement p=c.prepareStatement("SELECT booking_id FROM bookings WHERE booking_id=? FOR UPDATE")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Booking was not found.");}}
                String requestStatus;
                try(PreparedStatement p=c.prepareStatement("SELECT request_status FROM cancellation_requests WHERE request_id=? FOR UPDATE")){p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Cancellation request was not found.");requestStatus=r.getString(1);}}
                if(!"PENDING".equals(requestStatus))throw new SQLException("This request has already been reviewed.");
                if(approve)cancelBookingWithinTransaction(c,bookingId,userId);
                String newStatus=approve?"APPROVED":"REJECTED",response=approve?"Cancellation approved; time-tier refund recorded.":"Cancellation request declined by admin.";
                try(PreparedStatement p=c.prepareStatement("UPDATE cancellation_requests SET request_status=?,admin_response=?,reviewed_by=?,reviewed_at=CURRENT_TIMESTAMP WHERE request_id=?")){p.setString(1,newStatus);p.setString(2,response);p.setLong(3,adminId);p.setLong(4,requestId);p.executeUpdate();}
                c.commit();
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;throw new SQLException("Cancellation request could not be resolved.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public List<TicketInfo> payAndIssueTickets(long bookingId,long userId,String method) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                long flightId;String status;
                try(PreparedStatement p=c.prepareStatement("SELECT flight_id,status FROM bookings WHERE booking_id=? AND user_id=? FOR UPDATE")){p.setLong(1,bookingId);p.setLong(2,userId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Booking not found.");flightId=r.getLong(1);status=r.getString(2);}}
                if(!"PENDING".equals(status))throw new SQLException("This booking is no longer pending.");
                try(PreparedStatement p=c.prepareStatement("SELECT flight_id FROM flights WHERE flight_id=? FOR UPDATE")){p.setLong(1,flightId);p.executeQuery().close();}
                try(PreparedStatement p=c.prepareStatement("UPDATE payments SET payment_method=?,payment_status='PAID',payment_date=CURRENT_TIMESTAMP WHERE booking_id=?")){p.setString(1,method);p.setLong(2,bookingId);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("UPDATE bookings SET status='CONFIRMED' WHERE booking_id=?")){p.setLong(1,bookingId);p.executeUpdate();}
                List<TicketInfo> tickets=new ArrayList<>();
                try(PreparedStatement p=c.prepareStatement("SELECT t.ticket_number,p.name,t.seat_number FROM tickets t JOIN passengers p ON p.passenger_id=t.passenger_id WHERE t.booking_id=? ORDER BY p.passenger_id")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){while(r.next())tickets.add(new TicketInfo(r.getString(1),r.getString(2),r.getString(3)));}}
                if(tickets.isEmpty()){
                    int businessCapacity;BigDecimal economyPrice;
                    try(PreparedStatement f=c.prepareStatement("SELECT business_capacity,economy_ticket_price FROM flights WHERE flight_id=?")){f.setLong(1,flightId);try(ResultSet r=f.executeQuery()){r.next();businessCapacity=r.getInt(1);economyPrice=r.getBigDecimal(2);}}
                    try(PreparedStatement p=c.prepareStatement("SELECT passenger_id,name FROM passengers WHERE booking_id=? ORDER BY passenger_id")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){int n=businessCapacity+1;while(r.next()){String seat;do{seat=((n-1)/6+1)+String.valueOf((char)('A'+(n-1)%6));n++;}while(seatTaken(c,flightId,seat));String ticket="TKT-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);try(PreparedStatement insert=c.prepareStatement("INSERT INTO tickets(ticket_number,booking_id,flight_id,passenger_id,seat_number,cabin_class,ticket_price,ticket_status) VALUES(?,?,?,?,?,'ECONOMY',?,'ISSUED')")){insert.setString(1,ticket);insert.setLong(2,bookingId);insert.setLong(3,flightId);insert.setLong(4,r.getLong(1));insert.setString(5,seat);insert.setBigDecimal(6,economyPrice);insert.executeUpdate();}tickets.add(new TicketInfo(ticket,r.getString(2),seat));}}}
                }
                c.commit();return tickets;
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;throw new SQLException("Payment could not be completed.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public boolean cancelPending(long bookingId) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);try{long flightId;String status;try(PreparedStatement p=c.prepareStatement("SELECT flight_id,status FROM bookings WHERE booking_id=? FOR UPDATE")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(!r.next())return false;flightId=r.getLong(1);status=r.getString(2);}}if(!"PENDING".equals(status))return false;
                int[] cabinCounts=bookingCabinCounts(c,bookingId);int count=cabinCounts[0]+cabinCounts[1];
                try(PreparedStatement p=c.prepareStatement("UPDATE flights SET economy_available_seats=economy_available_seats+?,business_available_seats=business_available_seats+?,available_seats=available_seats+? WHERE flight_id=?")){p.setInt(1,cabinCounts[0]);p.setInt(2,cabinCounts[1]);p.setInt(3,count);p.setLong(4,flightId);p.executeUpdate();}try(PreparedStatement p=c.prepareStatement("UPDATE bookings SET status='CANCELLED' WHERE booking_id=?")){p.setLong(1,bookingId);p.executeUpdate();}try(PreparedStatement p=c.prepareStatement("UPDATE payments SET payment_status='FAILED' WHERE booking_id=?")){p.setLong(1,bookingId);p.executeUpdate();}try(PreparedStatement p=c.prepareStatement("UPDATE tickets SET ticket_status='CANCELLED',cancelled_at=CURRENT_TIMESTAMP(6) WHERE booking_id=?")){p.setLong(1,bookingId);p.executeUpdate();}c.commit();return true;
            }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }
    private boolean seatTaken(Connection c,long flightId,String seat) throws SQLException {try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id JOIN flights f ON f.flight_id=t.flight_id WHERE t.flight_id=? AND f.flight_id=? AND TRIM(UPPER(t.seat_number))=? AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' LIMIT 1")){p.setLong(1,flightId);p.setLong(2,flightId);p.setString(3,seat.trim().toUpperCase(Locale.ROOT));try(ResultSet r=p.executeQuery()){return r.next();}}}
    private int[] activeCabinCounts(Connection c,long flightId) throws SQLException {
        String sql="SELECT COUNT(DISTINCT CASE WHEN t.cabin_class='ECONOMY' THEN TRIM(UPPER(t.seat_number)) END),COUNT(DISTINCT CASE WHEN t.cabin_class='BUSINESS' THEN TRIM(UPPER(t.seat_number)) END) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=? AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$'";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,flightId);try(ResultSet r=p.executeQuery()){if(!r.next())return new int[]{0,0};return new int[]{r.getInt(1),r.getInt(2)};}}
    }
    private int[] bookingCabinCounts(Connection c,long bookingId) throws SQLException {
        int business=0,economy=0;boolean found=false;
        try(PreparedStatement p=c.prepareStatement("SELECT SUM(cabin_class='ECONOMY'),SUM(cabin_class='BUSINESS') FROM tickets WHERE booking_id=? AND ticket_status IN ('ISSUED','USED')")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(r.next()){economy=r.getInt(1);business=r.getInt(2);found=economy+business>0;}}}
        if(!found)try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM passengers WHERE booking_id=?")){p.setLong(1,bookingId);try(ResultSet r=p.executeQuery()){if(r.next())economy=r.getInt(1);}}
        return new int[]{economy,business};
    }
    private int seatOrdinal(String seat){try{int row=Integer.parseInt(seat.substring(0,seat.length()-1)),col=seat.charAt(seat.length()-1)-'A';if(row<1||col<0||col>5)return -1;return (row-1)*6+col+1;}catch(RuntimeException ex){return -1;}}
    private List<BookingSummary> summaries(String sql,long... params) throws SQLException {List<BookingSummary> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<params.length;i++)p.setLong(i+1,params[i]);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new BookingSummary(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7),r.getBigDecimal(8),r.getTimestamp(9).toLocalDateTime(),r.getTimestamp(10).toLocalDateTime(),r.getString(11)));}}return out;}
}
