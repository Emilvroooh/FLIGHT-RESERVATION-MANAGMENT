package com.flightbooking.service;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class VerificationIdValidator {
    public static final String PAN = "PAN Card";
    public static final String AADHAAR = "Aadhaar Card";
    public static final String PASSPORT = "Passport";
    private static final Map<String, Pattern> PATTERNS = Map.of(
            PAN, Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]"),
            AADHAAR, Pattern.compile("[0-9]{12}"),
            PASSPORT, Pattern.compile("(?:[0-9]{9}|[A-Z][0-9]{8})")
    );

    private VerificationIdValidator() {}

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValidType(String type) {
        return type != null && PATTERNS.containsKey(type);
    }

    public static boolean isValid(String type, String number) {
        Pattern pattern = type == null ? null : PATTERNS.get(type);
        return pattern != null && pattern.matcher(normalize(number)).matches();
    }

    public static String errorMessage(String type) {
        return switch (type == null ? "" : type) {
            case PAN -> "Invalid PAN format. Please check your entry.";
            case AADHAAR -> "Invalid Aadhaar format. Enter exactly 12 digits.";
            case PASSPORT -> "Invalid Passport format. Use 9 digits or 1 letter followed by 8 digits.";
            default -> "Choose a verification ID type.";
        };
    }
}
