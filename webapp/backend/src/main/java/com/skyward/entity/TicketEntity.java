package com.skyward.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="tickets",uniqueConstraints={@UniqueConstraint(name="uq_tickets_number",columnNames="ticket_number"),@UniqueConstraint(name="uq_tickets_passenger",columnNames="passenger_id")},indexes=@Index(name="idx_tickets_flight_status_seat",columnList="flight_id,ticket_status,seat_number"))
public class TicketEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ticket_id") private Long id;
 @Column(name="ticket_number",nullable=false,length=40,unique=true) private String ticketNumber;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false) private BookingEntity booking;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="flight_id",nullable=false) private FlightEntity flight;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="passenger_id",nullable=false,unique=true) private PassengerEntity passenger;
 @Column(name="seat_number",nullable=false,length=8) private String seatNumber;
 @Column(name="cabin_class",nullable=false,length=20) private String cabinClass="ECONOMY";
 @Column(name="ticket_price",nullable=false,precision=10,scale=2) private BigDecimal ticketPrice;
 @Column(name="ticket_status",nullable=false,length=24) private String ticketStatus="ISSUED";
 @Column(name="cancelled_at") private LocalDateTime cancelledAt;
 protected TicketEntity(){}public Long getId(){return id;}public String getTicketNumber(){return ticketNumber;}public String getSeatNumber(){return seatNumber;}public String getCabinClass(){return cabinClass;}public String getTicketStatus(){return ticketStatus;}public BigDecimal getTicketPrice(){return ticketPrice;}
}
