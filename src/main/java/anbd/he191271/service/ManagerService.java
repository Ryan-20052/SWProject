package anbd.he191271.service;

import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;

    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public Optional<Manager> login(String username, String password) {
        return managerRepository.findByUsername(username)
                .filter(c -> c.getPassword().equals(password));
    }
}
