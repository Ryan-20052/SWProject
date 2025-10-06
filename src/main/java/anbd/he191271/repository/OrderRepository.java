package anbd.he191271.repository;

import anbd.he191271.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByCode(String code);

    // ✅ Load luôn customer khi tìm theo code
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.code = :code")
    Optional<Order> findByCodeWithCustomer(@Param("code") String code);

}
