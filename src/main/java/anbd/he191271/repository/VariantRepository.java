package anbd.he191271.repository;

import anbd.he191271.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Integer> {
    List<Variant> findByProductId(long productId);
}
