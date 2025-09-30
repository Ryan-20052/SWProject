package anbd.he191271.controller;

import anbd.he191271.dto.RegisterRequest;
import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.EmailService;
import anbd.he191271.service.OtpService;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class RegisterController {

    private final OtpService otpService;
    private final EmailService emailService; // Service gửi mail
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(OtpService otpService, EmailService emailService, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    private RegisterRequest pendingUser;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) throws MessagingException {
        // Kiểm tra trùng email / username
        if (customerRepository.existsByEmail(request.getEmail())) {
            return "❌ Email already exists!";
        }
        if (customerRepository.existsByUsername(request.getUsername())) {
            return "❌ Username already exists!";
        }

        this.pendingUser = request; // Lưu tạm thông tin

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendEmail(request.getEmail(), "Your OTP Code", "Your OTP is: " + otp);

        return "✅ OTP has been sent to your email. It will expire in 5 minutes.";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email, @RequestParam String otp) {
        boolean valid = otpService.validateOtp(email, otp);

        if (valid && pendingUser != null && email.equals(pendingUser.getEmail())) {
            Customer customer = new Customer();
            customer.setName(pendingUser.getName());
            customer.setEmail(pendingUser.getEmail());
            customer.setUsername(pendingUser.getUsername());
            // 👉 Hash mật khẩu trước khi lưu
            String hashedPassword = passwordEncoder.encode(pendingUser.getPassword());
            customer.setPassword(hashedPassword);

            customerRepository.save(customer);

            pendingUser = null; // clear sau khi lưu

            return "🎉 Registration successful! Your account has been created.";
        } else {
            return "❌ Invalid or expired OTP!";
        }
    }
}