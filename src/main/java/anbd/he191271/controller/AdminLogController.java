package anbd.he191271.controller;

import anbd.he191271.entity.Admin_log;
import anbd.he191271.service.AdminLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {
    private final AdminLogService logService;

    public AdminLogController(AdminLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<List<Admin_log>> getLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

}