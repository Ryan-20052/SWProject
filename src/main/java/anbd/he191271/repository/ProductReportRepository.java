package anbd.he191271.repository;

import anbd.he191271.entity.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductReportRepository extends JpaRepository<ProductReport, Long>, JpaSpecificationExecutor<ProductReport> {

    List<ProductReport> findByStatus(String status);
    long countByStatus(String status);

    // Lọc theo status và title
    List<ProductReport> findByStatusAndTitle(String status, String title);

    // Lọc theo title
    List<ProductReport> findByTitle(String title);

    // Lọc theo khoảng ngày
    List<ProductReport> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Lọc theo status và khoảng ngày
    List<ProductReport> findByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    // Lọc theo title và khoảng ngày
    List<ProductReport> findByTitleAndCreatedAtBetween(String title, LocalDateTime start, LocalDateTime end);

    // Lọc theo status, title và khoảng ngày
    List<ProductReport> findByStatusAndTitleAndCreatedAtBetween(String status, String title, LocalDateTime start, LocalDateTime end);
}