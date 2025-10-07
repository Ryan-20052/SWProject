package anbd.he191271.service;

import anbd.he191271.entity.Categories;
import anbd.he191271.repository.CategoryRepository;
import anbd.he191271.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Categories> getAllCategories() {
        return categoryRepository.findAll();
    }
    public Categories  getCategoryById(int id) {
        return categoryRepository.findById( id).get();
    }
}
