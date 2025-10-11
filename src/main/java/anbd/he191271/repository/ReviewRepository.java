package anbd.he191271.repository;

import anbd.he191271.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}


