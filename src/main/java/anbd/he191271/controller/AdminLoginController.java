package anbd.he191271.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
public class AdminLoginController {

    // Hardcode tài khoản admin
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123";

    // API Login (POST /auth/admin/login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("admin", true); // lưu session

            Map<String, String> res = new HashMap<>();
            res.put("username", "admin");
            return ResponseEntity.ok(res);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // API Logout (GET /auth/admin/logout)
    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate(); // xoá session

        Map<String, String> res = new HashMap<>();
        res.put("status", "logged_out");
        return ResponseEntity.ok(res);
    }

    // API check login (dùng để bảo vệ trang admin nếu cần)
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("admin");
        Map<String, Object> res = new HashMap<>();
        res.put("isAdmin", isAdmin != null && isAdmin);
        return ResponseEntity.ok(res);
    }
}
