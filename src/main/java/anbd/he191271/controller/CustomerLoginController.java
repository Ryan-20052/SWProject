package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.service.CustomerService;

@RestController
@RequestMapping("/auth/customer")
public class CustomerLoginController {

    private final CustomerService customerService;

    public CustomerLoginController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public ResponseEntity<Customer> login(@RequestBody LoginRequest request) {
        return customerService.login(request.getUsername(), request.getPassword())
                .map(customer -> ResponseEntity.ok(customer))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }



}