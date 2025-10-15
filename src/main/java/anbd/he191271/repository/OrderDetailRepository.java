package anbd.he191271.repository;
import java.util.List;
import java.util.Optional;
import anbd.he191271.entity.Order;
import anbd.he191271.entity.OrderDetail;
import anbd.he191271.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    Optional<OrderDetail> findFirstByOrder(Order order);
    // JPQL: lấy product id và tổng số lượng đã bán, sắp xếp giảm dần
    @Query("SELECT od.variant.product.id, SUM(od.amount) " +
            "FROM OrderDetail od " +
            "GROUP BY od.variant.product.id " +
            "ORDER BY SUM(od.amount) DESC")
    List<Object[]> findTopProductIds(Pageable pageable);
    @Query("""
        SELECT od.variant.product.name, SUM(od.amount) AS totalSold
        FROM OrderDetail od
        GROUP BY od.variant.product.name
        ORDER BY totalSold DESC
    """)
    List<Object[]> findTopSellingProducts();

    List<OrderDetail> findByOrder(Order order);
}
