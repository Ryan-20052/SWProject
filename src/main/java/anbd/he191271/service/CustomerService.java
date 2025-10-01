package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
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

    public Optional<Customer> login(String username, String rawPassword) {
        return customerRepository.findByUsername(username)
                .filter(customer -> passwordEncoder.matches(rawPassword, customer.getPassword()));
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

    public void deleteCustomer(int id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Customer not found with id " + id);
        }
    }
}
