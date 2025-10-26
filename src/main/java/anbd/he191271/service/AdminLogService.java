package anbd.he191271.service;

import anbd.he191271.entity.Admin_log;
import anbd.he191271.repository.AdminLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Page<Admin_log> searchLogs(String action, String table,
                                      LocalDate startTime, LocalDate endTime,
                                      int page, int size, String sortBy, String order) {

        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        LocalDateTime start = (startTime != null) ? startTime.atStartOfDay() : null;
        LocalDateTime end = (endTime != null) ? endTime.plusDays(1).atStartOfDay() : null;

        return logRepository.searchLogs(
                action != null ? action : "",
                table != null ? table : "",
                start, end, pageable
        );
    }
}
