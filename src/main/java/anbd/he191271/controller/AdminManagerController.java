package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/managers")
public class AdminManagerController {

    private final ManagerService managerService;
    private final AdminLogService logService;

    public AdminManagerController(ManagerService managerService, AdminLogService logService) {
        this.managerService = managerService;
        this.logService = logService;
    }

    // Lấy danh sách tất cả managers
    @GetMapping
    public ResponseEntity<List<Manager>> getAllManagers() {
        return ResponseEntity.ok(managerService.findAll());
    }

    // Thêm manager mới
    @PostMapping
    public ResponseEntity<Manager> addManager(@RequestBody Manager manager) {
        Manager saved = managerService.save(manager);
        logService.saveLog("add","manager");
        return ResponseEntity.ok(saved);
    }

    // Xóa manager theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable int id) {
        logService.saveLog("delete manager name:"+managerService.getManagerById(id).getUsername(),"manager");
        managerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}