package com.skyward.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="flights",uniqueConstraints=@UniqueConstraint(name="uq_flights_number_departure",columnNames={"flight_number","departure_time"}))
public class FlightEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="flight_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="airline_id",nullable=false) private AirlineEntity airline;
 @Column(name="flight_number",nullable=false,length=20) private String flightNumber;
 @Column(name="departure_city",nullable=false,length=120) private String departureCity;
 @Column(name="destination_city",nullable=false,length=120) private String destinationCity;
 @Column(name="departure_time",nullable=false) private LocalDateTime departureTime;
 @Column(name="arrival_time",nullable=false) private LocalDateTime arrivalTime;
 @Column(name="available_seats",nullable=false) private int availableSeats;
 @Column(name="ticket_price",nullable=false,precision=10,scale=2) private BigDecimal ticketPrice;
 @Column(name="economy_capacity",nullable=false) private int economyCapacity;
 @Column(name="economy_available_seats",nullable=false) private int economyAvailableSeats;
 @Column(name="economy_ticket_price",nullable=false,precision=10,scale=2) private BigDecimal economyPrice;
 @Column(name="business_capacity",nullable=false) private int businessCapacity;
 @Column(name="business_available_seats",nullable=false) private int businessAvailableSeats;
 @Column(name="business_ticket_price",nullable=false,precision=10,scale=2) private BigDecimal businessPrice;
 protected FlightEntity(){}
 public Long getId(){return id;}public AirlineEntity getAirline(){return airline;}public String getFlightNumber(){return flightNumber;}public String getDepartureCity(){return departureCity;}public String getDestinationCity(){return destinationCity;}public LocalDateTime getDepartureTime(){return departureTime;}public LocalDateTime getArrivalTime(){return arrivalTime;}public int getEconomyCapacity(){return economyCapacity;}public int getBusinessCapacity(){return businessCapacity;}public int getEconomyAvailableSeats(){return economyAvailableSeats;}public int getBusinessAvailableSeats(){return businessAvailableSeats;}public BigDecimal getEconomyPrice(){return economyPrice;}public BigDecimal getBusinessPrice(){return businessPrice;}
}
