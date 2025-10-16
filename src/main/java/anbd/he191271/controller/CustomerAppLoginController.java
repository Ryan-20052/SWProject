package anbd.he191271.controller;


import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import anbd.he191271.service.CustomerService;
import anbd.he191271.service.EmailService;
import anbd.he191271.service.OtpService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/customer")
public class CustomerAppLoginController {

    private final CustomerService customerService;
    private final OtpService otpService;
    private final EmailService emailService;

    public CustomerAppLoginController(CustomerService customerService, OtpService otpService, EmailService emailService) {
        this.customerService = customerService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // 1️⃣ Bước 1: login → gửi OTP
    @PostMapping("/app-login")
    public ResponseEntity<?> appLogin(@RequestBody LoginRequest request) {
        Optional<Customer> opt = customerService.findByUsername(request.getUsername());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }

        Customer customer = opt.get();

        if ("BANNED".equalsIgnoreCase(customer.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("ok", false, "message", "Tài khoản bị khóa!"));
        }

        if (!customerService.checkPassword(request.getPassword(), customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "Sai mật khẩu!"));
        }

        // ✅ Sinh OTP và gửi mail
        String otp = otpService.generateOtp(customer.getEmail());
        try {
            emailService.sendEmail(
                    customer.getEmail(),
                    "Mã OTP đăng nhập SHINE SHOP",
                    "<p>Mã OTP của bạn là: <b>" + otp + "</b><br>Hiệu lực trong 5 phút.</p>"
            );
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "message", "Không thể gửi email OTP!"));
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "OTP đã được gửi tới email " + customer.getEmail(),
                "email", customer.getEmail()
        ));
    }

    // 2️⃣ Bước 2: xác thực OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        boolean valid = otpService.validateOtp(email, otp);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "OTP không hợp lệ hoặc đã hết hạn!"));
        }

        otpService.clearOtp(email);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Xác thực OTP thành công",
                "email", email
        ));
    }
}
