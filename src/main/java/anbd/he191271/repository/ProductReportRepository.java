package anbd.he191271.repository;

import anbd.he191271.entity.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {

    // Tìm báo cáo theo trạng thái
    List<ProductReport> findByStatus(String status);

    // Tìm báo cáo theo email
    List<ProductReport> findByEmail(String email);

    // Tìm báo cáo theo productId
    List<ProductReport> findByProductId(Long productId);

    // Đếm số lượng báo cáo theo trạng thái
    long countByStatus(String status);
}