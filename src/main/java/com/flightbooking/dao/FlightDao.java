package com.flightbooking.dao;

import com.flightbooking.config.DatabaseConnection;
import com.flightbooking.model.Airline;
import com.flightbooking.model.Flight;
import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class FlightDao {
    private static final String SELECT="SELECT f.flight_id,f.airline_id,a.airline_name,f.flight_number,f.departure_city,f.destination_city,f.departure_time,f.arrival_time,"+
            "GREATEST(f.economy_capacity-(SELECT COUNT(DISTINCT TRIM(UPPER(t.seat_number))) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=f.flight_id AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$' AND t.cabin_class='ECONOMY'),0)+"+
            "GREATEST(f.business_capacity-(SELECT COUNT(DISTINCT TRIM(UPPER(t.seat_number))) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=f.flight_id AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$' AND t.cabin_class='BUSINESS'),0) AS available_seats,"+
            "f.economy_capacity,f.business_capacity,"+
            "GREATEST(f.economy_capacity-(SELECT COUNT(DISTINCT TRIM(UPPER(t.seat_number))) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=f.flight_id AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$' AND t.cabin_class='ECONOMY'),0) AS economy_available_seats,"+
            "GREATEST(f.business_capacity-(SELECT COUNT(DISTINCT TRIM(UPPER(t.seat_number))) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=f.flight_id AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$' AND t.cabin_class='BUSINESS'),0) AS business_available_seats,"+
            "f.economy_ticket_price,f.business_ticket_price FROM flights f JOIN airlines a ON a.airline_id=f.airline_id ";
    public List<Flight> search(String from,String to,LocalDate date) throws SQLException {
        return search(from,to,date,null);
    }
    /** Searches by route/date and optionally caps the Economy fare shown as the base ticket price. */
    public List<Flight> search(String from,String to,LocalDate date,BigDecimal maxEconomyPrice) throws SQLException {
        updateStatuses();
        StringBuilder sql=new StringBuilder(SELECT+"WHERE f.departure_time>=NOW()");
        if(from!=null&&!from.isBlank())sql.append(" AND LOWER(f.departure_city) LIKE LOWER(?)");if(to!=null&&!to.isBlank())sql.append(" AND LOWER(f.destination_city) LIKE LOWER(?)");if(date!=null)sql.append(" AND f.departure_time>=? AND f.departure_time<?");if(maxEconomyPrice!=null)sql.append(" AND f.economy_ticket_price<=?");sql.append(" ORDER BY f.departure_time");
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql.toString())){int i=1;if(from!=null&&!from.isBlank())p.setString(i++,"%"+from.trim()+"%");if(to!=null&&!to.isBlank())p.setString(i++,"%"+to.trim()+"%");if(date!=null){p.setTimestamp(i++,Timestamp.valueOf(date.atStartOfDay()));p.setTimestamp(i++,Timestamp.valueOf(date.plusDays(1).atStartOfDay()));}if(maxEconomyPrice!=null)p.setBigDecimal(i,maxEconomyPrice);List<Flight> results=flights(p);results.removeIf(f->f.getAvailableSeats()<=0);return results;}
    }
    public List<Flight> listAll() throws SQLException {updateStatuses();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(SELECT+"ORDER BY f.departure_time")){return flights(p);}}
    public void updateStatuses() throws SQLException {
        String expected="CASE WHEN arrival_time<=CURRENT_TIMESTAMP THEN 'LANDED' WHEN departure_time<=CURRENT_TIMESTAMP THEN 'DEPARTED' WHEN departure_time<=DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 MINUTE) THEN 'CLOSING_SOON' ELSE 'SCHEDULED' END";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE flights SET flight_status="+expected+" WHERE flight_status<>"+expected)){p.executeUpdate();}
    }
    public List<Airline> airlines() throws SQLException {List<Airline> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT airline_id,airline_name FROM airlines ORDER BY airline_name");ResultSet r=p.executeQuery()){while(r.next())out.add(new Airline(r.getLong(1),r.getString(2)));}return out;}
    public void save(long id,long airlineId,String number,String from,String to,LocalDateTime depart,LocalDateTime arrive,int economyCapacity,int businessCapacity,BigDecimal economyPrice,BigDecimal businessPrice) throws SQLException {
        if(economyCapacity<0||businessCapacity<0||economyCapacity+businessCapacity<=0)throw new IllegalArgumentException("Enter a positive cabin seat capacity.");
        if(businessCapacity>=6&&businessCapacity%6!=0)throw new IllegalArgumentException("Business Class capacity must be a whole number of six-seat cabin rows.");
        if(economyPrice==null||businessPrice==null||economyPrice.signum()<0||businessPrice.compareTo(economyPrice.multiply(new BigDecimal("2.5")))<0||businessPrice.compareTo(economyPrice.multiply(new BigDecimal("3.0")))>0)throw new IllegalArgumentException("Business fare must be between 2.5× and 3× the Economy fare.");
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int economyTaken=0,businessTaken=0,currentEconomyCapacity=-1,currentBusinessCapacity=-1;
                if(id>0){
                    try(PreparedStatement lock=c.prepareStatement("SELECT economy_capacity,business_capacity FROM flights WHERE flight_id=? FOR UPDATE")){lock.setLong(1,id);try(ResultSet r=lock.executeQuery()){if(!r.next())throw new SQLException("Flight was not found.");currentEconomyCapacity=r.getInt(1);currentBusinessCapacity=r.getInt(2);}}
                    try(PreparedStatement p=c.prepareStatement("SELECT cabin_class,COUNT(DISTINCT TRIM(UPPER(t.seat_number))) FROM tickets t JOIN bookings b ON b.booking_id=t.booking_id WHERE t.flight_id=? AND b.status IN ('PENDING','CONFIRMED') AND t.ticket_status='ISSUED' AND TRIM(UPPER(t.seat_number)) REGEXP '^[1-9][0-9]{0,2}[A-F]$' GROUP BY cabin_class")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){if("BUSINESS".equals(r.getString(1)))businessTaken=r.getInt(2);else economyTaken=r.getInt(2);}}}
                    if(economyCapacity<economyTaken||businessCapacity<businessTaken)throw new SQLException("Cabin capacity cannot be lower than the number of assigned active tickets (Economy: "+economyTaken+", Business: "+businessTaken+").");
                    if(economyTaken+businessTaken>0&&(economyCapacity!=currentEconomyCapacity||businessCapacity!=currentBusinessCapacity))throw new SQLException("Cabin capacities cannot be changed after seats have been booked; fares and flight details may still be edited.");
                }
                String sql=id>0?"UPDATE flights SET airline_id=?,flight_number=?,departure_city=?,destination_city=?,departure_time=?,arrival_time=?,economy_capacity=?,economy_available_seats=?,economy_ticket_price=?,business_capacity=?,business_available_seats=?,business_ticket_price=?,available_seats=?,ticket_price=? WHERE flight_id=?":"INSERT INTO flights(airline_id,flight_number,departure_city,destination_city,departure_time,arrival_time,economy_capacity,economy_available_seats,economy_ticket_price,business_capacity,business_available_seats,business_ticket_price,available_seats,ticket_price) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try(PreparedStatement p=c.prepareStatement(sql)){
                    int i=1;p.setLong(i++,airlineId);p.setString(i++,number.trim());p.setString(i++,from.trim());p.setString(i++,to.trim());p.setTimestamp(i++,Timestamp.valueOf(depart));p.setTimestamp(i++,Timestamp.valueOf(arrive));
                    p.setInt(i++,economyCapacity);p.setInt(i++,economyCapacity-economyTaken);p.setBigDecimal(i++,economyPrice);p.setInt(i++,businessCapacity);p.setInt(i++,businessCapacity-businessTaken);p.setBigDecimal(i++,businessPrice);p.setInt(i++,economyCapacity+businessCapacity-economyTaken-businessTaken);p.setBigDecimal(i++,economyPrice);if(id>0)p.setLong(i,id);p.executeUpdate();
                }
                c.commit();
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException sql)throw sql;if(ex instanceof IllegalArgumentException ia)throw ia;throw new SQLException("Flight could not be saved.",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public void delete(long id) throws SQLException {try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM flights WHERE flight_id=?")){p.setLong(1,id);p.executeUpdate();}}
    private List<Flight> flights(PreparedStatement p) throws SQLException {List<Flight> out=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Flight(r.getLong("flight_id"),r.getLong("airline_id"),r.getString("airline_name"),r.getString("flight_number"),r.getString("departure_city"),r.getString("destination_city"),r.getTimestamp("departure_time").toLocalDateTime(),r.getTimestamp("arrival_time").toLocalDateTime(),r.getInt("available_seats"),r.getInt("economy_capacity"),r.getInt("business_capacity"),r.getInt("economy_available_seats"),r.getInt("business_available_seats"),r.getBigDecimal("economy_ticket_price"),r.getBigDecimal("business_ticket_price")));}return out;}
}
