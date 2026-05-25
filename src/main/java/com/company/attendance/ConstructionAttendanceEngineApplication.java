package com.company.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Construction Worker Attendance &amp; Overtime Settlement Engine.
 *
 * <p>This application provides enterprise-grade REST APIs for managing construction
 * worker attendance records, real-time active-worker tracking via Redis, automatic
 * tiered overtime calculation, and monthly overtime settlement processing.</p>
 */
@SpringBootApplication
@EnableScheduling
public class ConstructionAttendanceEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstructionAttendanceEngineApplication.class, args);
    }
}
