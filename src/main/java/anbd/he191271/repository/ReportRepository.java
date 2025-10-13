package anbd.he191271.repository;

import anbd.he191271.entity.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ProductReport, Long> {
}
