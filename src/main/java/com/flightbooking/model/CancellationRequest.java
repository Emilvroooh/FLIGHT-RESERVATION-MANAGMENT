package com.flightbooking.model;

import java.time.LocalDateTime;

public class CancellationRequest {
    private final long id,bookingId;
    private final String customerName,email,bookingStatus,flightNumber,departureCity,destinationCity,requestStatus;
    private final LocalDateTime requestedAt,departureTime;
    public CancellationRequest(long id,long bookingId,String customerName,String email,String bookingStatus,String flightNumber,String departureCity,String destinationCity,String requestStatus,LocalDateTime requestedAt,LocalDateTime departureTime){this.id=id;this.bookingId=bookingId;this.customerName=customerName;this.email=email;this.bookingStatus=bookingStatus;this.flightNumber=flightNumber;this.departureCity=departureCity;this.destinationCity=destinationCity;this.requestStatus=requestStatus;this.requestedAt=requestedAt;this.departureTime=departureTime;}
    public long getId(){return id;} public long getBookingId(){return bookingId;} public String getCustomerName(){return customerName;} public String getEmail(){return email;} public String getBookingStatus(){return bookingStatus;} public String getFlightNumber(){return flightNumber;} public String getDepartureCity(){return departureCity;} public String getDestinationCity(){return destinationCity;} public String getRequestStatus(){return requestStatus;} public LocalDateTime getRequestedAt(){return requestedAt;} public LocalDateTime getDepartureTime(){return departureTime;}
}
