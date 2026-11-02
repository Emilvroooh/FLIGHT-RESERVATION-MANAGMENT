package com.skyward.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="cancellation_requests",indexes=@Index(name="idx_cancellation_requests_status_date",columnList="request_status,request_date"))
public class CancellationRequestEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="request_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false) private BookingEntity booking;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @Column(name="request_date",insertable=false,updatable=false) private LocalDateTime requestDate;
 @Column(name="request_status",nullable=false,length=24) private String requestStatus="PENDING";
 @Column(name="admin_response",length=500) private String adminResponse;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reviewed_by") private AdminEntity reviewedBy;
 @Column(name="reviewed_at") private LocalDateTime reviewedAt;
 protected CancellationRequestEntity(){}public Long getId(){return id;}public BookingEntity getBooking(){return booking;}public UserEntity getUser(){return user;}public String getRequestStatus(){return requestStatus;}public void setRequestStatus(String value){requestStatus=value;}public void setAdminResponse(String value){adminResponse=value;}public void setReviewedBy(AdminEntity value){reviewedBy=value;}public void setReviewedAt(LocalDateTime value){reviewedAt=value;}
}
