package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> login(String username, String password) {
        return customerRepository.findByUsername(username)
                .filter(c -> c.getPassword().equals(password));
    }

    public Optional<Customer> getById(int id) {
        return customerRepository.findById(id);
    }
    public Customer getCustomerById(int id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String getAvatarOrInitial(Customer customer) {
        if (customer.getAvatar() != null && !customer.getAvatar().isEmpty()) {
            return "/uploads/" + new File(customer.getAvatar()).getName();
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
