package com.flightbooking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BoardingPass {
    private final String ticketNumber, passengerName, gender, passportNumber, verificationIdType, verificationIdNumber,
            flightNumber, airline, destinationCity, seatNumber, cabinClass;
    private final int age;
    private final long bookingId;
    private final BigDecimal ticketPrice;
    private final LocalDateTime departureTime, arrivalTime;

    public BoardingPass(String ticketNumber, String passengerName, int age, String gender, String passportNumber,
                        String verificationIdType, String verificationIdNumber, String flightNumber, String airline, String destinationCity, LocalDateTime departureTime,
                        LocalDateTime arrivalTime, String seatNumber, String cabinClass, BigDecimal ticketPrice, long bookingId) {
        this.ticketNumber=ticketNumber; this.passengerName=passengerName; this.age=age; this.gender=gender;
        this.passportNumber=passportNumber; this.verificationIdType=verificationIdType; this.verificationIdNumber=verificationIdNumber;
        this.flightNumber=flightNumber; this.airline=airline;
        this.destinationCity=destinationCity; this.departureTime=departureTime; this.arrivalTime=arrivalTime;
        this.seatNumber=seatNumber; this.cabinClass=cabinClass; this.ticketPrice=ticketPrice; this.bookingId=bookingId;
    }
    public String getTicketNumber(){return ticketNumber;} public String getPassengerName(){return passengerName;}
    public int getAge(){return age;} public String getGender(){return gender;} public String getPassportNumber(){return passportNumber;}
    public String getVerificationIdType(){return verificationIdType;} public String getVerificationIdNumber(){return verificationIdNumber;}
    public String getFlightNumber(){return flightNumber;} public String getAirline(){return airline;}
    public String getDestinationCity(){return destinationCity;} public LocalDateTime getDepartureTime(){return departureTime;}
    public LocalDateTime getArrivalTime(){return arrivalTime;} public String getSeatNumber(){return seatNumber;} public String getCabinClass(){return cabinClass;}
    public BigDecimal getTicketPrice(){return ticketPrice;} public long getBookingId(){return bookingId;}
    @Override public String toString(){return passengerName+" · "+flightNumber+" · Seat "+seatNumber+" · SB-"+bookingId;}
}
