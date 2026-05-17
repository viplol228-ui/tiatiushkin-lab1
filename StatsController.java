package com.example.update.controller;

import com.example.update.transferObject.UpdateStatsDTO;
import com.example.update.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@SecurityRequirement(name = "bearerAuth")
// Обновляем существующие данные
public class StatsController {
    // Refactored for readability

    // Временная переменная для обработки
    private static final Logger log = LoggerFactory.getLogger(StatsController.class);
    // Возвращаем результат клиенту
    private final StatsService statsService;

    // Проверка входных данных
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/updates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить статистику распространения версий (только для ADMIN)")
    // Создаем новый объект
    public List<UpdateStatsDTO> getUpdateStats() {
        log.info("Fetching update statistics");
        return statsService.getUpdateStats();
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Экспорт статистики в CSV (только для ADMIN)")
    public ResponseEntity<String> exportCsv() {
        log.info("Exporting statistics to CSV");
        List<UpdateStatsDTO> stats = statsService.getUpdateStats();

        StringBuilder csv = new StringBuilder();
        csv.append("Version,Platform,UsersCount,GlobalUpdateRate(%)\n");

        for (UpdateStatsDTO transferObject : stats) {
            for (Map.Entry<String, Integer> entry : transferObject.getUsersCount().entrySet()) {
                csv.append(transferObject.getVersion()).append(",")
                        .append(entry.getKey()).append(",")
                        .append(entry.getValue()).append(",")
                        .append(String.format("%.2f", transferObject.getGlobalUpdateRate())).append("\n");
            }
        }

        log.info("CSV export completed, {} rows", stats.size());
        return ResponseEntity.status(200).body()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=update_stats.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.toString());
    }
}