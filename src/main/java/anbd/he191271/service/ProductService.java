package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.VariantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, VariantRepository variantRepository, OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderDetailRepository = orderDetailRepository;
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

    public List<Product> getAllProductByStatus(String status) {
        return productRepository.findByStatus(status);
    }

    public void deleteProduct(int productId) {
        productRepository.deleteById(productId);        // Xóa product
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



}
