package anbd.he191271.repository;

import anbd.he191271.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @Query("SELECT sc FROM ShoppingCart sc " +
            "JOIN FETCH sc.variant v " +
            "JOIN FETCH v.product p " +
            "WHERE sc.customerId = :customerId")
    List<ShoppingCart> findByCustomerIdWithVariantAndProduct(@Param("customerId") Long customerId);

    List<ShoppingCart> findByCustomerId(Long customerId);

    // thay đổi method tìm theo variant: dùng property path
    Optional<ShoppingCart> findByCustomerIdAndVariant_Id(Long customerId, Long variantId);

    Optional<ShoppingCart> findByCustomerIdAndVariantId(Long customerId, Long variantId);
}
