package anbd.he191271.controller;

import anbd.he191271.dto.CustomerDTO;
import anbd.he191271.entity.Customer;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.CustomerService;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*") // Cho phép frontend JS gọi
public class AdminCustomerController {

    private final CustomerService customerService;
    private final AdminLogService logService;

    public AdminCustomerController(CustomerService customerService, AdminLogService logService) {
        this.customerService = customerService;
        this.logService = logService;
    }


    // Lấy danh sách customer
    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(c -> new CustomerDTO(c.getId(), c.getUsername(), c.getEmail(),c.getStatus()))
                .toList();
    }


    // 🚫 Ban customer
    @PutMapping("/{id}/ban")
    public ResponseEntity<String> banCustomer(@PathVariable int id) {
        var customer = customerService.getCustomerById(id);
        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");

        if ("BANNED".equals(customer.getStatus()))
            return ResponseEntity.badRequest().body("Customer already banned");

        customerService.banCustomer(id);
        logService.saveLog("Ban customer: " + customer.getUsername(), "customer");
        return ResponseEntity.ok("Customer banned successfully");
    }

    // ✅ Unban
    @PutMapping("/{id}/unban")
    public ResponseEntity<String> unbanCustomer(@PathVariable int id) {
        var customer = customerService.getCustomerById(id);
        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");

        if (!"BANNED".equals(customer.getStatus()))
            return ResponseEntity.badRequest().body("Customer is not banned");

        customerService.unbanCustomer(id);
        logService.saveLog("Unban customer: " + customer.getUsername(), "customer");
        return ResponseEntity.ok("Customer unbanned successfully");
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Customer> result = customerService.searchCustomers(username, email, status, page, size);
        List<CustomerDTO> dtoList = result.getContent().stream()
                .map(c -> new CustomerDTO(c.getId(), c.getUsername(), c.getEmail(), c.getStatus()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "content", dtoList,
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        ));
    }
}