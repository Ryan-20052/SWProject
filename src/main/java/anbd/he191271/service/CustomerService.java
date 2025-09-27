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

    public Optional<Customer> login(String username, String password) {
        return customerRepository.findByUsername(username)
                .filter(c -> c.getPassword().equals(password));
        // 🚨 Lưu ý: sản phẩm thật thì dùng BCryptPasswordEncoder
    }
}

