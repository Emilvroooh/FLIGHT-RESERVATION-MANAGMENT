package com.flightbooking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/flight_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private DatabaseConnection() {}
    public static Connection getConnection() throws SQLException {
        String url = env("FLIGHT_DB_URL", DEFAULT_URL);
        String user = env("FLIGHT_DB_USER", "flight_app");
        String password = System.getenv("FLIGHT_DB_PASSWORD");
        if (password == null || password.isBlank()) throw new SQLException("Set FLIGHT_DB_PASSWORD before starting the app.");
        return DriverManager.getConnection(url, user, password);
    }
    private static String env(String key, String fallback) { String value=System.getenv(key); return value==null||value.isBlank()?fallback:value; }
}
