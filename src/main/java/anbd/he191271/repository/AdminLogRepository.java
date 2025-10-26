package anbd.he191271.repository;

import anbd.he191271.entity.Admin_log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

public interface AdminLogRepository extends JpaRepository<Admin_log, Integer> {

    @Query("SELECT l FROM Admin_log l " +
            "WHERE (:action = '' OR LOWER(l.action) LIKE LOWER(CONCAT('%', :action, '%'))) " +
            "AND (:table = '' OR LOWER(l.table_affected) LIKE LOWER(CONCAT('%', :table, '%'))) " +
            "AND (:start IS NULL OR l.time >= :start) " +
            "AND (:end IS NULL OR l.time < :end)")
    Page<Admin_log> searchLogs(@Param("action") String action,
                               @Param("table") String table,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               Pageable pageable);

}
