package anbd.he191271.repository;
import anbd.he191271.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("""
    SELECT c FROM Customer c
    WHERE (:username = '' OR :username IS NULL OR LOWER(c.username) LIKE LOWER(CONCAT('%', :username, '%')))
      AND (:email = '' OR :email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))
      AND (:status = '' OR :status IS NULL OR c.status = :status)
""")
    Page<Customer> searchCustomers(@Param("username") String username,
                                   @Param("email") String email,
                                   @Param("status") String status,
                                   Pageable pageable);
}