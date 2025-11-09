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


    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    @Query(
            value = "SELECT * FROM products p WHERE p.categories_id = :catId",
            countQuery = "SELECT count(*) FROM products p WHERE p.categories_id = :catId",
            nativeQuery = true
    )
    Page<Product> findByCategoryIdNative(@Param("catId") Integer catId, Pageable pageable);


    List<Product> findByStatus(String status);        // hiện có
    List<Product> findByCategoryId(Integer id);       // hiện có

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE %:keyword% OR LOWER(p.description) LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);


}

