package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.DashboardMetricsDTO;
import cl.reciclajelitoral.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<DashboardMetricsDTO> getMetrics(
            @RequestParam(required = false, defaultValue = "ALL") String scope,
            @RequestParam(required = false, defaultValue = "HISTORIC") String period,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String region
    ) {
        return ResponseEntity.ok(adminDashboardService.getMetrics(scope, period, userId, comunaId, role, region));
    }
}
