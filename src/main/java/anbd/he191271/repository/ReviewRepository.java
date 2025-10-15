package anbd.he191271.repository;

import anbd.he191271.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ sửa import đúng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByCustomer_IdAndProduct_Id(int customerId, int productId);

    List<Review> findByProduct_Id(int productId);

    @Query("""
        SELECT r.product.name, AVG(r.rating) AS avgRating
        FROM Review r
        GROUP BY r.product.name
        HAVING COUNT(r.id) >= 1
        ORDER BY avgRating DESC
    """)
    List<Object[]> findTopRatedProducts();

    // ✅ Truy vấn động cho lọc theo sao, ngày, có ảnh hay không
    @Query("""
        SELECT r FROM Review r
        WHERE r.product.id = :productId
        AND (:rating IS NULL OR r.rating = :rating)
        AND (:hasImage IS NULL OR r.hasImage = :hasImage)
        AND (:startDate IS NULL OR r.createdAt >= :startDate)
        AND (:endDate IS NULL OR r.createdAt <= :endDate)
        ORDER BY r.createdAt DESC
    """)
    Page<Review> findFilteredReviews(
            @Param("productId") int productId,
            @Param("rating") Integer rating,
            @Param("hasImage") Boolean hasImage,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable
    );
}
