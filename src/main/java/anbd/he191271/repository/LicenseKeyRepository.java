package anbd.he191271.repository;

import anbd.he191271.entity.LicenseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Integer> {

    @Query("SELECT lk FROM LicenseKey lk WHERE lk.orderDetail.order.customer.id = :customerId")
    List<LicenseKey> findAllByCustomerId(@Param("customerId") int customerId);
    Optional<LicenseKey> findByKey(String key);
    List<LicenseKey> findByStatusAndExpiredAtBefore(String status, Date date);
}
