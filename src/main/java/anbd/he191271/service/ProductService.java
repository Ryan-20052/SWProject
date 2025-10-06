package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    public ProductService(ProductRepository productRepository, VariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    // Lấy tất cả sản phẩm
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    public Product findProductById(final int id) {
        return productRepository.findById(id).get();
    }


    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }
    public void saveProduct(Product product) {
        productRepository.save(product);
    }



    public void deleteProduct(int productId) {
        productRepository.deleteById(productId);        // Xóa product
    }
    public Variant findVariantById(Long id) {
        return variantRepository.findById(Math.toIntExact(id)).orElse(null);
    }
    public Product getProductId(int id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            return productOpt.get();
        }
        return null; // hoặc throw new RuntimeException("Product not found");
    }


}
