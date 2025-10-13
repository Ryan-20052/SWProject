package anbd.he191271.repository;

import anbd.he191271.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerReportRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
}
