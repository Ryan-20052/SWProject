package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


import anbd.he191271.entity.Manager;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/managers")
@CrossOrigin(origins = "*") // Cho phép gọi từ frontend JS
public class AdminManagerController {

    private final ManagerService managerService;
    private final AdminLogService logService;
    private final CustomerService customerService;

    public AdminManagerController(ManagerService managerService, AdminLogService logService, CustomerService customerService) {
        this.managerService = managerService;
        this.logService = logService;
        this.customerService = customerService;
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
            // Kiểm tra email trùng
            if (managerService.isEmailExists(manager.getEmail(), null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "EMAIL_EXISTS", "message", "Email đã tồn tại trong hệ thống"));
            }

            // Kiểm tra username trùng
            if (managerService.isUsernameExists(manager.getUsername(), null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "USERNAME_EXISTS", "message", "Username đã tồn tại trong hệ thống"));
            }

            Manager saved = managerService.save(manager);
            logService.saveLog("🟢 Thêm Manager: " + saved.getUsername(), "manager");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "SYSTEM_ERROR", "message", "Không thể thêm Manager: " + e.getMessage()));
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

    @GetMapping("/search")
    public ResponseEntity<?> searchManagers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(managerService.searchManagers(username, email, status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getManagerById(@PathVariable int id) {
        try {
            Manager manager = managerService.getManagerById(id);
            if (manager == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Manager không tồn tại"));
            }
            return ResponseEntity.ok(manager);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lấy thông tin Manager: " + e.getMessage()));
        }
    }

    // ✅ Cập nhật manager (ĐÃ SỬA)
    @PutMapping(value = "/{id}/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateManagerForm(
            @PathVariable int id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "username", required = false) String username,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "status", required = false) String status,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        try {
            Manager manager = managerService.getManagerById(id);
            if (manager == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "NOT_FOUND", "message", "Manager không tồn tại"));
            }

            // Kiểm tra email trùng (nếu có thay đổi)
            if (email != null && !email.equals(manager.getEmail())) {
                if (managerService.isEmailExists(email, id)&& customerService.isUsernameExists(username, id)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "EMAIL_EXISTS", "message", "Email đã tồn tại trong hệ thống"));
                }
            }

            // Kiểm tra username trùng (nếu có thay đổi)
            if (username != null && !username.equals(manager.getUsername())) {
                if (managerService.isUsernameExists(username, id)&&customerService.isUsernameExists(username, id)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "USERNAME_EXISTS", "message", "Username đã tồn tại trong hệ thống"));
                }
            }

            // Validation định dạng
            if (email != null && !email.isEmpty()) {
                if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "INVALID_EMAIL", "message", "Email không hợp lệ"));
                }
            }

            if (phone != null && !phone.isEmpty()) {
                if (!phone.matches("^[0-9]{10,11}$")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "INVALID_PHONE", "message", "Số điện thoại không hợp lệ"));
                }
            }

            // Cập nhật thông tin
            if (name != null) manager.setName(name);
            if (username != null) manager.setUsername(username);
            if (email != null) manager.setEmail(email);
            if (phone != null) manager.setPhone(phone);
            if (status != null) manager.setStatus(status);

            // Xử lý avatar
            if (avatar != null && !avatar.isEmpty()) {
                if (avatar.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "AVATAR_TOO_LARGE", "message", "Avatar quá lớn (tối đa 5MB)"));
                }
                String contentType = avatar.getContentType();
                if (!Arrays.asList("image/jpeg", "image/png", "image/gif").contains(contentType)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "INVALID_AVATAR_TYPE", "message", "Chỉ chấp nhận file ảnh (JPEG, PNG, GIF)"));
                }
                manager.setAvatar(avatar.getBytes());
            }

            Manager updatedManager = managerService.save(manager);
            logService.saveLog("Cập nhật Manager: " + updatedManager.getUsername(), "manager");

            return ResponseEntity.ok(updatedManager);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "SYSTEM_ERROR", "message", "Lỗi cập nhật: " + e.getMessage()));
        }
    }
}