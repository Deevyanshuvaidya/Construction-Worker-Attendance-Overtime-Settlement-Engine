package com.company.attendance.overtime.controller;

import com.company.attendance.overtime.dto.OvertimeSummaryDTO;
import com.company.attendance.overtime.service.OvertimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing worker overtime summaries and settlements.
 */
@RestController
@RequestMapping("/api/v1/overtime")
@Tag(name = "Overtime Management", description = "APIs for worker monthly overtime summary and payout settlement")
public class OvertimeController {

    private static final Logger log = LoggerFactory.getLogger(OvertimeController.class);

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    /**
     * Retrieves the monthly overtime summary for a worker.
     *
     * @param workerId the worker's ID
     * @param year the year
     * @param month the month (1-12)
     * @return the overtime summary
     */
    @GetMapping("/summary/{workerId}")
    @Operation(summary = "Get worker monthly overtime summary", description = "Retrieves aggregated overtime hours, payout calculations, and daily breakdown for a worker in a specific month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Worker not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OvertimeSummaryDTO> getOvertimeSummary(
            @PathVariable @Parameter(description = "Worker's unique database ID") Long workerId,
            @RequestParam @Parameter(description = "Year (e.g. 2026)") int year,
            @RequestParam @Parameter(description = "Month (1-12)") int month) {
        log.info("API request to fetch overtime summary: workerId={}, year={}, month={}", workerId, year, month);
        OvertimeSummaryDTO summary = overtimeService.getOvertimeSummary(workerId, year, month);
        return ResponseEntity.ok(summary);
    }

    /**
     * Settles the pending monthly overtime entries for a worker.
     *
     * @param workerId the worker's ID
     * @param year the year
     * @param month the month (1-12)
     * @return empty response with status 200 OK or 204 No Content
     */
    @PostMapping("/settle/{workerId}")
    @Operation(summary = "Settle monthly overtime for a worker", description = "Marks all pending overtime entries for a specific completed month as SETTLED. Current month cannot be settled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overtime settled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Worker or pending overtime entries not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity (e.g., settling current month)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> settleOvertime(
            @PathVariable @Parameter(description = "Worker's unique database ID") Long workerId,
            @RequestParam @Parameter(description = "Year (e.g. 2026)") int year,
            @RequestParam @Parameter(description = "Month (1-12)") int month) {
        log.info("API request to settle overtime: workerId={}, year={}, month={}", workerId, year, month);
        overtimeService.settleOvertime(workerId, year, month);
        return ResponseEntity.ok().build();
    }
}
