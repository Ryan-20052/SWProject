package anbd.he191271.controller;

import anbd.he191271.dto.CustomerDTO;
import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.service.CustomerService;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/auth/customer")
public class CustomerLoginController {

    private final CustomerService customerService;

    public CustomerLoginController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        // 1️⃣ Tìm theo username
        Optional<Customer> opt = customerService.findByUsername(request.getUsername());

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }

        Customer customer = opt.get();

        // 2️⃣ Kiểm tra trạng thái tài khoản
        if ("BANNED".equalsIgnoreCase(customer.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "BANNED", "message", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ!"));
        }

        if ("DELETED".equalsIgnoreCase(customer.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "DELETED", "message", "Tài khoản này đã bị xóa."));
        }

        // 3️⃣ Kiểm tra mật khẩu (đã mã hóa)
        if (!customerService.checkPassword(request.getPassword(), customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }

        // ✅ Thành công → Lưu session
        session.setAttribute("customer", customer);
        return ResponseEntity.ok(customer);
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