package anbd.he191271.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength = 10 là mặc định; tăng lên 12 nếu muốn an toàn hơn nhưng chậm hơn
        return new BCryptPasswordEncoder(10);
    }

    // ... cấu hình security khác (AuthManager, HttpSecurity...) ...
}