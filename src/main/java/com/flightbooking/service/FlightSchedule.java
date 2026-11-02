package com.flightbooking.service;

import com.flightbooking.model.Flight;
import java.time.LocalDateTime;

public final class FlightSchedule {
    private FlightSchedule() {}
    public static String status(LocalDateTime departure, LocalDateTime arrival, LocalDateTime now) {
        if (!now.isBefore(arrival)) return "LANDED";
        if (!now.isBefore(departure)) return "DEPARTED";
        return now.isAfter(departure.minusMinutes(30)) || now.isEqual(departure.minusMinutes(30))
                ? "CLOSING_SOON" : "SCHEDULED";
    }
    public static boolean bookingOpen(Flight flight, LocalDateTime now) {
        return now.isBefore(flight.getDepartureTime())
                && !now.isAfter(flight.getDepartureTime().minusMinutes(30));
    }
    public static String message(Flight flight, LocalDateTime now) {
        return switch (status(flight.getDepartureTime(), flight.getArrivalTime(), now)) {
            case "LANDED" -> "Successfully Landed";
            case "DEPARTED" -> "Flight Departed";
            default -> "Booking Closed - Flight Departing Soon";
        };
    }
    public static String label(LocalDateTime departure, LocalDateTime arrival, LocalDateTime now) {
        return switch (status(departure, arrival, now)) {
            case "LANDED" -> "Successfully Landed";
            case "DEPARTED" -> "Already Departed";
            case "CLOSING_SOON" -> "Closing Soon";
            default -> "Scheduled";
        };
    }
}
