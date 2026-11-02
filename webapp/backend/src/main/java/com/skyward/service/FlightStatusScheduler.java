package com.skyward.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Persists departure status changes every 15 seconds; booking checks remain transaction-time checks. */
@Component
public class FlightStatusScheduler {
    private final JdbcTemplate db;
    public FlightStatusScheduler(JdbcTemplate db) { this.db = db; }

    @Scheduled(fixedDelay = 15_000)
    public void refreshStatuses() {
        String status = "CASE WHEN arrival_time<=CURRENT_TIMESTAMP THEN 'LANDED' "
                + "WHEN departure_time<=CURRENT_TIMESTAMP THEN 'DEPARTED' "
                + "WHEN departure_time<=DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 MINUTE) THEN 'CLOSING_SOON' ELSE 'SCHEDULED' END";
        db.update("UPDATE flights SET flight_status=" + status + " WHERE flight_status<>" + status);
    }
}
