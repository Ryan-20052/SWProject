package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<Customer> getById(int id) {
        return customerRepository.findById(id);
    }

    public Customer getCustomerById(int id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Nếu có avatar thì trả về true (client sẽ load /customer/avatar/{id})
    // Nếu không thì lấy chữ cái đầu
    public String getAvatarOrInitial(Customer customer) {
        if (customer.getAvatar() != null && customer.getAvatar().length > 0) {
            return "/customer/avatar/" + customer.getId();
        }
        if (customer.getName() != null && !customer.getName().isEmpty()) {
            return customer.getName().substring(0, 1).toUpperCase();
        }
        return "?";
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void banCustomer(int id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        c.setStatus("BANNED");
        customerRepository.save(c);
    }

    public void unbanCustomer(int id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        c.setStatus("ACTIVE");
        customerRepository.save(c);
    }
    public Page<Customer> searchCustomers(String username, String email, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return customerRepository.searchCustomers(username, email, status, pageable);
    }

}
