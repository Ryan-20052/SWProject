package anbd.he191271.service;

import anbd.he191271.entity.ManagerLog;
import anbd.he191271.repository.ManagerLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerLogService {
    @Autowired
    private final ManagerLogRepository managerLogRepository;
    public ManagerLogService(ManagerLogRepository managerLogRepository) {
        this.managerLogRepository = managerLogRepository;
    }
    public void save(ManagerLog managerLog) {
        managerLogRepository.save(managerLog);
    }
}
