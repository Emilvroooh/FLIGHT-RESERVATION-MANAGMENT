package com.flightbooking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundSummary {
    private final long bookingId;
    private final String flightNumber, departureCity, destinationCity, reason;
    private final LocalDateTime departureTime, cancelledAt;
    private final long secondsBeforeDeparture;
    private final BigDecimal originalAmount, refundPercentage, refundedAmount;

    public RefundSummary(long bookingId,String flightNumber,String departureCity,String destinationCity,
                         LocalDateTime departureTime,LocalDateTime cancelledAt,long secondsBeforeDeparture,
                         BigDecimal originalAmount,BigDecimal refundPercentage,BigDecimal refundedAmount,String reason){
        this.bookingId=bookingId;this.flightNumber=flightNumber;this.departureCity=departureCity;this.destinationCity=destinationCity;
        this.departureTime=departureTime;this.cancelledAt=cancelledAt;this.secondsBeforeDeparture=secondsBeforeDeparture;
        this.originalAmount=originalAmount;this.refundPercentage=refundPercentage;this.refundedAmount=refundedAmount;this.reason=reason;
    }
    public long getBookingId(){return bookingId;} public String getFlightNumber(){return flightNumber;}
    public String getDepartureCity(){return departureCity;} public String getDestinationCity(){return destinationCity;}
    public LocalDateTime getDepartureTime(){return departureTime;} public LocalDateTime getCancelledAt(){return cancelledAt;}
    public long getSecondsBeforeDeparture(){return secondsBeforeDeparture;} public BigDecimal getOriginalAmount(){return originalAmount;}
    public BigDecimal getRefundPercentage(){return refundPercentage;} public BigDecimal getRefundedAmount(){return refundedAmount;}
    public String getReason(){return reason;}
}
