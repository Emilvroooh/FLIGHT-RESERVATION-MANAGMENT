package com.flightbooking.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Flight {
    private final long id, airlineId;
    private final String airlineName, flightNumber, departureCity, destinationCity;
    private final LocalDateTime departureTime, arrivalTime;
    private final int availableSeats, economyCapacity, businessCapacity, economyAvailableSeats, businessAvailableSeats;
    private final BigDecimal economyPrice, businessPrice;

    public Flight(long id,long airlineId,String airlineName,String flightNumber,String departureCity,String destinationCity,
                  LocalDateTime departureTime,LocalDateTime arrivalTime,int availableSeats,int economyCapacity,
                  int businessCapacity,int economyAvailableSeats,int businessAvailableSeats,BigDecimal economyPrice,BigDecimal businessPrice){
        this.id=id;this.airlineId=airlineId;this.airlineName=airlineName;this.flightNumber=flightNumber;
        this.departureCity=departureCity;this.destinationCity=destinationCity;this.departureTime=departureTime;
        this.arrivalTime=arrivalTime;this.availableSeats=availableSeats;this.economyCapacity=economyCapacity;
        this.businessCapacity=businessCapacity;this.economyAvailableSeats=economyAvailableSeats;
        this.businessAvailableSeats=businessAvailableSeats;this.economyPrice=economyPrice;this.businessPrice=businessPrice;
    }
    public long getId(){return id;} public long getAirlineId(){return airlineId;} public String getAirlineName(){return airlineName;}
    public String getFlightNumber(){return flightNumber;} public String getDepartureCity(){return departureCity;}
    public String getDestinationCity(){return destinationCity;} public LocalDateTime getDepartureTime(){return departureTime;}
    public LocalDateTime getArrivalTime(){return arrivalTime;} public int getAvailableSeats(){return availableSeats;}
    public int getEconomyCapacity(){return economyCapacity;} public int getBusinessCapacity(){return businessCapacity;}
    public int getEconomyAvailableSeats(){return economyAvailableSeats;} public int getBusinessAvailableSeats(){return businessAvailableSeats;}
    public BigDecimal getEconomyPrice(){return economyPrice;} public BigDecimal getBusinessPrice(){return businessPrice;}
    /** Legacy alias retained for older UI references. */ public BigDecimal getTicketPrice(){return economyPrice;}
    @Override public String toString(){return flightNumber+" — "+departureCity+" to "+destinationCity;}
}
