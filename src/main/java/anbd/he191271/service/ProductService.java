package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.VariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository<Product, Integer> productRepository;
    private final VariantRepository variantRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository<Product, Integer> productRepository, VariantRepository variantRepository, OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    // Lấy tất cả sản phẩm (không phân trang)
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

    public void saveAll(List<Product> productList) {
        productRepository.saveAll(productList);
    }

    public List<Product> getAllProductByStatus(String status) {
        return productRepository.findByStatus(status, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    public void deleteProduct(int productId) {
        productRepository.deleteById(productId);
    }

    // Lấy top N sản phẩm nhiều nhất (theo tổng amount trong order_detail)
    public List<Product> getBestSellingProducts(int limit) {
        List<Object[]> rows = orderDetailRepository.findTopProductIds(PageRequest.of(0, limit));
        List<Product> best = new ArrayList<>();

        for (Object[] r : rows) {
            Integer productId = (Integer) r[0];
            Optional<Product> p = productRepository.findById(productId);

            p.ifPresent(product -> {
                if ("available".equalsIgnoreCase(product.getStatus())) {
                    best.add(product);
                }
            });
        }

        return best;
    }

    /* ========== PHÂN TRANG ========== */

    /**
     * Trả về Page<Product> theo status
     */
    public Page<Product> getAllProductByStatusPage(String status, int page, int size) {
        return productRepository.findByStatus(status, PageRequest.of(page, size));
    }

    /**
     * Trả về Page<Product> theo category id
     */
    public Page<Product> getProductsByCategoryPage(Integer categoryId, int page, int size) {
        return productRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
    }

    public Variant findVariantById(int variantId) {
        return variantRepository.findById(variantId).get();
    }


}