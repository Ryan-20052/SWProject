package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    // Constructor injection (khuyên dùng)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Lấy tất cả sản phẩm
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    public Product findProductById(final int id) {
        return productRepository.findById(id).get();
    }
}
