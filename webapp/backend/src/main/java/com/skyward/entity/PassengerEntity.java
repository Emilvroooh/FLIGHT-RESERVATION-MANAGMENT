package com.skyward.entity;
import jakarta.persistence.*;
@Entity @Table(name="passengers")
public class PassengerEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="passenger_id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false) private BookingEntity booking;
 @Column(nullable=false,length=120) private String name;
 @Column(nullable=false) private int age;
 @Column(nullable=false,length=24) private String gender;
 @Column(name="passport_no",length=32) private String passportNumber;
 @Column(name="verification_id_type",length=20) private String verificationIdType;
 @Column(name="verification_id_number",length=32) private String verificationIdNumber;
 protected PassengerEntity(){}public PassengerEntity(BookingEntity booking,String name,int age,String gender,String passportNumber,String verificationIdType,String verificationIdNumber){this.booking=booking;this.name=name;this.age=age;this.gender=gender;this.passportNumber=passportNumber;this.verificationIdType=verificationIdType;this.verificationIdNumber=verificationIdNumber;}
 public Long getId(){return id;}public BookingEntity getBooking(){return booking;}public String getName(){return name;}public int getAge(){return age;}public String getGender(){return gender;}public String getPassportNumber(){return passportNumber;}public String getVerificationIdType(){return verificationIdType;}public String getVerificationIdNumber(){return verificationIdNumber;}
}
