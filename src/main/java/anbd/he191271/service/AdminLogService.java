package anbd.he191271.service;

import anbd.he191271.entity.Admin_log;
import anbd.he191271.repository.AdminLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminLogService {
    private final AdminLogRepository logRepository;

    public AdminLogService(AdminLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void saveLog(String action, String tableAffected) {
        logRepository.save(new Admin_log(action, tableAffected));
    }

    public List<Admin_log> getAllLogs() {
        return logRepository.findAll();
    }
}
