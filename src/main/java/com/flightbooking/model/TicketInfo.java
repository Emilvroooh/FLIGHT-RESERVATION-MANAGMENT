package com.flightbooking.model;
public class TicketInfo {
    private final String ticketNumber,passengerName,seatNumber;
    public TicketInfo(String ticketNumber,String passengerName,String seatNumber){this.ticketNumber=ticketNumber;this.passengerName=passengerName;this.seatNumber=seatNumber;}
    public String getTicketNumber(){return ticketNumber;} public String getPassengerName(){return passengerName;} public String getSeatNumber(){return seatNumber;}
}
