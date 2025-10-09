package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import anbd.he191271.entity.Manager;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/managers")
@CrossOrigin(origins = "*") // Cho phép gọi từ frontend JS
public class AdminManagerController {

    private final ManagerService managerService;
    private final AdminLogService logService;

    public AdminManagerController(ManagerService managerService, AdminLogService logService) {
        this.managerService = managerService;
        this.logService = logService;
    }

    // ✅ Lấy danh sách tất cả managers
    @GetMapping
    public ResponseEntity<List<Manager>> getAllManagers() {
        return ResponseEntity.ok(managerService.findAll());
    }

    // ✅ Thêm manager mới
    @PostMapping
    public ResponseEntity<?> addManager(@RequestBody Manager manager) {
        try {
            Manager saved = managerService.save(manager);
            logService.saveLog("🟢 Thêm Manager: " + saved.getUsername(), "manager");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Không thể thêm Manager: " + e.getMessage()));
        }
    }

    // ✅ Ban (khóa) manager
    @PutMapping("/{id}/ban")
    public ResponseEntity<?> banManager(@PathVariable int id) {
        try {
            managerService.banManager(id);
            Manager banned = managerService.getManagerById(id);
            logService.saveLog("Ban Manager: " + banned.getUsername(), "manager");
            return ResponseEntity.ok(Map.of("message", "Manager đã bị khóa thành công."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi khóa Manager: " + e.getMessage()));
        }
    }

    // ✅ Unban (mở khóa) manager
    @PutMapping("/{id}/unban")
    public ResponseEntity<?> unbanManager(@PathVariable int id) {
        try {
            managerService.unbanManager(id);
            Manager active = managerService.getManagerById(id);
            logService.saveLog("Unban Manager: " + active.getUsername(), "manager");
            return ResponseEntity.ok(Map.of("message", "Manager đã được mở khóa thành công."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi mở khóa Manager: " + e.getMessage()));
        }
    }
}