package anbd.he191271.repository;

import anbd.he191271.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {
    List<Categories> findByStatus(String status);
}
