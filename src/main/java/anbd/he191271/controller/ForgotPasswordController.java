package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.EmailService;
import anbd.he191271.service.OtpService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class ForgotPasswordController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(OtpService otpService, EmailService emailService, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Bước 1: Nhập email
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return customerRepository.findByEmail(email)
                .map(customer -> {
                    try {
                        String otp = otpService.generateOtp(email);
                        emailService.sendEmail(email, "Reset Password OTP", "Your OTP is: " + otp);
                        return ResponseEntity.ok("✅ OTP sent to your email. It will expire in 5 minutes.");
                    } catch (MessagingException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("❌ Failed to send email: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ This email is not registered in our system!"));
    }

    // Bước 2: Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean valid = otpService.validateOtp(email, otp);
        return valid ? "OTP verified! You can now reset password." : "❌ Invalid or expired OTP!";
    }

    // Bước 3: Reset mật khẩu
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String otp) {
        boolean valid = otpService.validateOtp(email, otp);
        if (!valid) {
            return "❌ Invalid or expired OTP!";
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Email not found!"));

        String hashedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(hashedPassword);
        customerRepository.save(customer);

        otpService.clearOtp(email);

        return "🎉 Password has been reset successfully!";
    }
}