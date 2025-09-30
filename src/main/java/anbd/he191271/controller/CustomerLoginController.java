package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import anbd.he191271.service.CustomerService;

@Controller
@RequestMapping("/auth/customer")
public class CustomerLoginController {

    private final CustomerService customerService;

    public CustomerLoginController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    @ResponseBody // vẫn trả JSON
    public ResponseEntity<Customer> login(@RequestBody LoginRequest request, HttpSession session) {
        return customerService.login(request.getUsername(), request.getPassword())
                .map(customer -> {
                    session.setAttribute("customer", customer);
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home/homepage";
    }
}
