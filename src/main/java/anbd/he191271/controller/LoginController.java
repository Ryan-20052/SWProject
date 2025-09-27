package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.service.CustomerService;
import anbd.he191271.repository.CustomerRepository;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final CustomerService customerService;

    public LoginController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public ResponseEntity<Customer> login(@RequestBody LoginRequest request) {
        return customerService.login(request.getUsername(), request.getPassword())
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }



}