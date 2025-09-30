package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.io.File;
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

    public String getAvatarOrInitial(Customer customer) {
        if (customer.getAvatar() != null && !customer.getAvatar().isEmpty()) {
            return "/uploads/" + new File(customer.getAvatar()).getName();
        }
        if (customer.getName() != null && !customer.getName().isEmpty()) {
            return customer.getName().substring(0, 1).toUpperCase();
        }
        return "?";
    }
}
