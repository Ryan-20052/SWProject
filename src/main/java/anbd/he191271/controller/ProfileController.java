package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class ProfileController {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public ProfileController(CustomerRepository customerRepository,
                             CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    // Lấy thông tin customer theo id
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable int id) {
        return customerService.getById(id)
                .map(customer -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", customer.getId());
                    response.put("name", customer.getName());
                    response.put("email", customer.getEmail());
                    response.put("dob", customer.getDob());
                    response.put("avatar", customerService.getAvatarOrInitial(customer));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Update thông tin (name, email, dob)
    // Update thông tin (name, email, dob + avatar nếu có)
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateProfile(
            @PathVariable int id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String dob,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile
    ) throws IOException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(name);
        customer.setEmail(email);
        if (dob != null && !dob.isEmpty()) {
            customer.setDob(LocalDate.parse(dob));
        }

        // Xử lý upload avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String uploadDir = "src/main/resources/static/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = avatarFile.getOriginalFilename();
            String filePath = uploadDir + fileName;
            avatarFile.transferTo(new File(filePath));

            customer.setAvatar("/uploads/" + fileName);
        }

        customerRepository.save(customer);
        return ResponseEntity.ok(customer);
    }

}
