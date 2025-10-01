package anbd.he191271.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength = 10 là mặc định; tăng lên 12 nếu muốn an toàn hơn nhưng chậm hơn
        return new BCryptPasswordEncoder(10);
    }

    // ... cấu hình security khác (AuthManager, HttpSecurity...) ...
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (nếu bạn dùng API REST)
                .csrf(csrf -> csrf.disable())
                // Cho phép tất cả request (không chặn nữa)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // Tắt form login mặc định
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}