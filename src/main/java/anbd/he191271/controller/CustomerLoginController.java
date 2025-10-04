package anbd.he191271.controller;

import anbd.he191271.dto.CustomerDTO;
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
    @ResponseBody
    public ResponseEntity<CustomerDTO> login(@RequestBody LoginRequest request, HttpSession session) {
        return customerService.login(request.getUsername(), request.getPassword())
                .map(customer -> {
                    session.setAttribute("customer", customer); // lưu nguyên bản vào session

                    // chỉ trả JSON gọn gàng ra FE
                    CustomerDTO dto = new CustomerDTO(
                            customer.getId(),
                            customer.getUsername(),
                            customer.getEmail()
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home/homepage";
    }
}