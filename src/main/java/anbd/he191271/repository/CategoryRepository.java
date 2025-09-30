package anbd.he191271.repository;

import anbd.he191271.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {
}
