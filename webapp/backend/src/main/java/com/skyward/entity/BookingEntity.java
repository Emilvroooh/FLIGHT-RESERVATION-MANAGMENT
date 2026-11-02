package com.skyward.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="bookings")
public class BookingEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="booking_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="flight_id",nullable=false) private FlightEntity flight;
 @Column(name="booking_date",insertable=false,updatable=false) private LocalDateTime bookingDate;
 @Column(nullable=false,length=24) private String status="PENDING";
 @Column(name="total_amount",nullable=false,precision=10,scale=2) private BigDecimal totalAmount;
 @OneToMany(mappedBy="booking",cascade=CascadeType.ALL,orphanRemoval=true) private List<PassengerEntity> passengers=new ArrayList<>();
 @OneToOne(mappedBy="booking",cascade=CascadeType.ALL,orphanRemoval=true) private PaymentEntity payment;
 protected BookingEntity(){}
 public BookingEntity(UserEntity user,FlightEntity flight,BigDecimal total){this.user=user;this.flight=flight;this.totalAmount=total;}
 public Long getId(){return id;}public UserEntity getUser(){return user;}public FlightEntity getFlight(){return flight;}public LocalDateTime getBookingDate(){return bookingDate;}public String getStatus(){return status;}public BigDecimal getTotalAmount(){return totalAmount;}public List<PassengerEntity> getPassengers(){return passengers;}public void setStatus(String status){this.status=status;}
}
