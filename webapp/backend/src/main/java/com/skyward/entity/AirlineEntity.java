package com.skyward.entity;
import jakarta.persistence.*;
@Entity @Table(name="airlines")
public class AirlineEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="airline_id") private Long id;
 @Column(name="airline_name",nullable=false,length=120,unique=true) private String name;
 @Column(nullable=false,length=100) private String country;
 @Column(name="contact_number",length=32) private String contactNumber;
 protected AirlineEntity(){} public Long getId(){return id;}public String getName(){return name;}public String getCountry(){return country;}public String getContactNumber(){return contactNumber;}
}
