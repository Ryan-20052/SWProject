package anbd.he191271.service;

import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Constructor
    public ManagerService(ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Lấy Manager theo ID
    public Manager getManagerById(int id) {
        return managerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }

    // ✅ Lấy toàn bộ Manager
    public List<Manager> findAll() {
        return managerRepository.findAll();
    }

    // ✅ Tạo mới Manager (mã hóa mật khẩu)
    public Manager save(Manager manager) {
        manager.setPassword(passwordEncoder.encode(manager.getPassword()));
        if (manager.getStatus() == null || manager.getStatus().isBlank()) {
            manager.setStatus("ACTIVE");
        }
        return managerRepository.save(manager);
    }

    // ✅ Hàm login mới — chỉ check username, controller sẽ xử lý logic
    public Optional<Manager> findByUsername(String username) {
        return managerRepository.findByUsername(username);
    }

    // ✅ Kiểm tra mật khẩu (đã mã hóa)
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ✅ Ban (khóa) tài khoản manager
    public void banManager(int id) {
        Manager manager = getManagerById(id);
        if ("BANNED".equalsIgnoreCase(manager.getStatus())) {
            throw new IllegalStateException("Manager is already banned");
        }
        manager.setStatus("BANNED");
        managerRepository.save(manager);
    }

    // ✅ Unban (mở khóa) tài khoản manager
    public void unbanManager(int id) {
        Manager manager = getManagerById(id);
        if (!"BANNED".equalsIgnoreCase(manager.getStatus())) {
            throw new IllegalStateException("Manager is not banned");
        }
        manager.setStatus("ACTIVE");
        managerRepository.save(manager);
    }


}