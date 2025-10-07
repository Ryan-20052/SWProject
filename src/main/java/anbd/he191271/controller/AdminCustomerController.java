package anbd.he191271.controller;

import anbd.he191271.dto.CustomerDTO;
import anbd.he191271.entity.Customer;
import anbd.he191271.service.AdminLogService;
import anbd.he191271.service.CustomerService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
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
                .map(c -> new CustomerDTO(c.getId(), c.getUsername(), c.getEmail()))
                .toList();
    }


    // Xóa customer theo id
    @DeleteMapping("/{id}")
    public String deleteCustomer(@PathVariable int id) {
        logService.saveLog("delete customer name:"+customerService.getCustomerById(id).getUsername(),"customer");
        customerService.deleteCustomer(id);
        return "Customer deleted successfully";
    }
}