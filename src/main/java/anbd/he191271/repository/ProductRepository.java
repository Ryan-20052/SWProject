package anbd.he191271.repository;

import anbd.he191271.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByStatus(String status);
    // nativeQuery — thay 'products' bằng tên bảng thực tế nếu khác
    @Query(value = "SELECT * FROM products p WHERE p.categories_id = :catId", nativeQuery = true)
    List<Product> findByCategoryIdNative(@Param("catId") Integer catId);

    List<Product> findByCategoryId(Integer id);
}
