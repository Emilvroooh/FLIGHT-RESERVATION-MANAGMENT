package com.flightbooking.model;

/** One active passenger/ticket row in an administrator's pre-flight manifest. */
public record PassengerManifestEntry(long bookingId, String name, int age, String gender,
                                     String verificationIdType, String verificationIdNumber,
                                     String seatNumber, String cabinClass) {
    public String maskedVerificationId() {
        if (verificationIdNumber == null || verificationIdNumber.isBlank()) return "—";
        String value = verificationIdNumber.trim();
        return "•".repeat(Math.max(0, value.length() - 4)) + value.substring(Math.max(0, value.length() - 4));
    }
}
