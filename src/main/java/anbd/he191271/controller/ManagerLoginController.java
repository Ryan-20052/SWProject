package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Manager;
import anbd.he191271.service.CustomerService;
import anbd.he191271.service.ManagerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/manager")
public class ManagerLoginController {

    private final ManagerService managerService;

    public ManagerLoginController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        // 1️⃣ Tìm theo username
        Optional<Manager> opt = managerService.findByUsername(request.getUsername());

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }

        Manager manager = opt.get();

        // 2️⃣ Kiểm tra trạng thái
        if ("BANNED".equalsIgnoreCase(manager.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "BANNED", "message", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin."));
        }

        if ("DELETED".equalsIgnoreCase(manager.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "DELETED", "message", "Tài khoản này đã bị xóa."));
        }

        // 3️⃣ Kiểm tra mật khẩu (đã mã hóa)
        if (!managerService.checkPassword(request.getPassword(), manager.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }

        // ✅ Thành công
        session.setAttribute("manager", manager);
        return ResponseEntity.ok(manager);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity
                .status(HttpStatus.FOUND) // 302
                .header(HttpHeaders.LOCATION, "/home/homepage")
                .build();
    }
}
