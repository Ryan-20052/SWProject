package anbd.he191271.repository;

import anbd.he191271.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository<P, I extends Number> extends JpaRepository<Product, Integer> {

    // -----------------------
    // Spring Data pageable methods (recommended)
    // -----------------------

    // Tìm theo status với phân trang
    Page<Product> findByStatus(String status, Pageable pageable);

    // Tìm theo category id với phân trang (derived query)
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    // -----------------------
    // Native query với pageable (nếu bạn muốn dùng native SQL)
    // Khi dùng nativeQuery + pageable, cần cung cấp countQuery để Spring tính tổng trang
    // -----------------------
    @Query(
            value = "SELECT * FROM products p WHERE p.categories_id = :catId",
            countQuery = "SELECT count(*) FROM products p WHERE p.categories_id = :catId",
            nativeQuery = true
    )
    Page<Product> findByCategoryIdNative(@Param("catId") Integer catId, Pageable pageable);

    // -----------------------
    // Phương thức cũ (giữ để tương thích với code hiện tại nếu còn dùng)
    // -----------------------
    List<Product> findByStatus(String status);        // hiện có
    List<Product> findByCategoryId(Integer id);       // hiện có

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE %:keyword% OR LOWER(p.description) LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
}
