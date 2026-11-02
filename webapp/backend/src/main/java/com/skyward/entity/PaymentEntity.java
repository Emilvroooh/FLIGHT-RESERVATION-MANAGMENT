package com.skyward.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="payments")
public class PaymentEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="payment_id") private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false,unique=true) private BookingEntity booking;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal amount;
 @Column(name="payment_date") private LocalDateTime paymentDate;
 @Column(name="payment_method",nullable=false,length=32) private String paymentMethod="CARD";
 @Column(name="payment_status",nullable=false,length=24) private String paymentStatus="PENDING";
 @Column(name="refund_amount",nullable=false,precision=10,scale=2) private BigDecimal refundAmount=BigDecimal.ZERO;
 @Column(name="refund_date") private LocalDateTime refundDate;
 protected PaymentEntity(){}public PaymentEntity(BookingEntity booking,BigDecimal amount){this.booking=booking;this.amount=amount;}
 public Long getId(){return id;}public BookingEntity getBooking(){return booking;}public BigDecimal getAmount(){return amount;}public LocalDateTime getPaymentDate(){return paymentDate;}public String getPaymentMethod(){return paymentMethod;}public String getPaymentStatus(){return paymentStatus;}public BigDecimal getRefundAmount(){return refundAmount;}
}
