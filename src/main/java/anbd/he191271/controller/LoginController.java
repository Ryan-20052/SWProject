package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.service.CustomerService;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final CustomerService customerService;

    public LoginController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        boolean success = customerService.login(request.getUsername(), request.getPassword());
        if (success) {
            return ResponseEntity.ok("Login success!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password!");
        }
    }
}