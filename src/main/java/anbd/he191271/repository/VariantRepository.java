package anbd.he191271.repository;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Integer> {
    List<Variant> findByProduct(Product product);

}
