package anbd.he191271.service;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.VariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<Product> getProductsWithFilters(Integer categoryId,
                                                String search,
                                                Integer minPrice,
                                                Integer maxPrice,
                                                String sort,
                                                int page,
                                                int size) {

        Pageable pageable = createPageable(sort, page, size);

        // Xử lý tìm kiếm
        String searchTerm = (search != null && !search.trim().isEmpty()) ? "%" + search.toLowerCase() + "%" : null;

        return productRepository.findWithFilters(
                categoryId,
                searchTerm,
                minPrice,
                maxPrice,
                pageable);
    }

    private Pageable createPageable(String sort, int page, int size) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size);
        }

        switch (sort) {
            case "name_asc":
                return PageRequest.of(page, size, Sort.by("name").ascending());
            case "name_desc":
                return PageRequest.of(page, size, Sort.by("name").descending());
            case "price_asc":
                return PageRequest.of(page, size, Sort.by(
                        org.springframework.data.domain.Sort.Order.asc("variants.price")
                ));
            case "price_desc":
                return PageRequest.of(page, size, Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("variants.price")
                ));
            default:
                return PageRequest.of(page, size);
        }
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
        //tìm các sản phẩm được mua nhiều nhất.
        List<Object[]> rows = orderDetailRepository.findTopProductIds(PageRequest.of(0, limit));
        List<Product> best = new ArrayList<>();
 //vòng for duyệt qua top 4 sản phẩm để check status available
        for (Object[] r : rows) {
            Integer productId = (Integer) r[0];
            //rows = [
            //    [12, 450],   // Product #12 bán 450 cái
            //    [5, 300],    // Product #5 bán 300 cái
            //    [9, 150]     // Product #9 bán 150 cái
            //
            //r[0]: productId
            //
            //r[1]: tổng số lượng bán (nếu truy vấn có cột này)
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