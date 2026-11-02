package com.skyward.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="booking_cancellations")
public class BookingCancellationEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="cancellation_id") private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false,unique=true) private BookingEntity booking;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="flight_id",nullable=false) private FlightEntity flight;
 @Column(name="flight_number",nullable=false,length=20) private String flightNumber;
 @Column(name="departure_city",nullable=false,length=120) private String departureCity;
 @Column(name="destination_city",nullable=false,length=120) private String destinationCity;
 @Column(name="departure_time",nullable=false) private LocalDateTime departureTime;
 @Column(name="cancelled_at",nullable=false) private LocalDateTime cancelledAt;
 @Column(name="seconds_before_departure",nullable=false) private long secondsBeforeDeparture;
 @Column(name="original_amount",nullable=false,precision=10,scale=2) private BigDecimal originalAmount;
 @Column(name="refund_percentage",nullable=false,precision=5,scale=2) private BigDecimal refundPercentage;
 @Column(name="refunded_amount",nullable=false,precision=10,scale=2) private BigDecimal refundedAmount;
 @Column(name="refund_reason",nullable=false,length=240) private String refundReason;
 protected BookingCancellationEntity(){}public Long getId(){return id;}public BigDecimal getOriginalAmount(){return originalAmount;}public BigDecimal getRefundedAmount(){return refundedAmount;}public BigDecimal getRefundPercentage(){return refundPercentage;}public String getRefundReason(){return refundReason;}
}
