package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public Manager getManagerById(int id) {
        return managerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("manager not found"));
    }
    public ManagerService(ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Manager> login(String username, String rawPassword) {
        return managerRepository.findByUsername(username)
                .filter(manager -> passwordEncoder.matches(rawPassword, manager.getPassword()));
    }
    public List<Manager> findAll() {
        return managerRepository.findAll();
    }

    public Manager save(Manager manager) {
        // 👉 Hash password trước khi lưu
        String rawPassword = manager.getPassword();
        manager.setPassword(passwordEncoder.encode(rawPassword));
        return managerRepository.save(manager);
    }

    public void deleteById(int id) {
        managerRepository.deleteById(id);
    }
}
