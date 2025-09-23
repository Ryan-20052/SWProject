package anbd.he191271.service;


import anbd.he191271.entity.Customer;
import org.springframework.stereotype.Service;
import anbd.he191271.repository.CustomerRepository;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public boolean login(String username, String password) {
        Optional<Customer> customer = customerRepository.findByUsername(username);
        return customer.isPresent() && customer.get().getPassword().equals(password);
        // ❗ Với production: dùng BCryptPasswordEncoder.matches() thay vì equals()
    }
}
