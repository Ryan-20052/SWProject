package anbd.he191271.repository;
import java.util.Optional;
import anbd.he191271.entity.Order;
import anbd.he191271.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    Optional<OrderDetail> findFirstByOrder(Order order);
}
