package anbd.he191271.repository;

import anbd.he191271.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByCode(String code);

    // ✅ Load luôn customer khi tìm theo code
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.code = :code")
    Optional<Order> findByCodeWithCustomer(@Param("code") String code);

    // Lấy orders theo khoảng thời gian
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Doanh thu tổng theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'PAID'")
    Long getTotalRevenueByPeriod(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    // Doanh thu theo kỳ trước (tháng trước/tuần trước/ngày trước)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'PAID'")
    Long getRevenuePreviousPeriod(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Doanh thu theo product và khoảng thời gian
    @Query("SELECT p.name, SUM(od.amount * v.price) as revenue " +
            "FROM OrderDetail od " +
            "JOIN od.variant v " +
            "JOIN v.product p " +
            "JOIN od.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'PAID' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY revenue DESC")
    List<Object[]> getRevenueByProduct(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Doanh thu theo category và khoảng thời gian
    @Query("SELECT c.name, SUM(od.amount * v.price) as revenue " +
            "FROM OrderDetail od " +
            "JOIN od.variant v " +
            "JOIN v.product p " +
            "JOIN p.category c " +
            "JOIN od.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'PAID' " +
            "GROUP BY c.id, c.name " +
            "ORDER BY revenue DESC")
    List<Object[]> getRevenueByCategory(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Doanh thu theo thời gian (ngày/tuần/tháng)
    @Query("SELECT FUNCTION('DATE', o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'PAID' " +
            "GROUP BY FUNCTION('DATE', o.orderDate) " +
            "ORDER BY FUNCTION('DATE', o.orderDate)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT YEAR(o.orderDate), WEEK(o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'PAID' " +
            "GROUP BY YEAR(o.orderDate), WEEK(o.orderDate) " +
            "ORDER BY YEAR(o.orderDate), WEEK(o.orderDate)")
    List<Object[]> getWeeklyRevenue(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'PAID' " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
            "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}
