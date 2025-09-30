package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
public class AdminLoginController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String ADMIN_USERNAME = "admin";
        String ADMIN_PASSWORD = "admin1234";

        Map<String, Object> response = new HashMap<>();

        if (ADMIN_USERNAME.equals(request.getUsername())
                && ADMIN_PASSWORD.equals(request.getPassword())) {
            response.put("status", "success");
            response.put("message", "Admin login success!");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid username or password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}