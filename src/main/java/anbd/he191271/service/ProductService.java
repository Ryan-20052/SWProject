package anbd.he191271.service;

import anbd.he191271.dto.ProductDTO;
import anbd.he191271.dto.VariantDTO;
import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.repository.ProductRepository;
import anbd.he191271.repository.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    public ProductService(ProductRepository productRepository, VariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            List<Variant> variants = variantRepository.findByProductId(product.getId());
            List<VariantDTO> variantDTOs = variants.stream()
                    .map(v -> new VariantDTO(v.getId(), v.getName(), v.getPrice(), v.getDuration()))
                    .collect(Collectors.toList());
            return new ProductDTO(product.getId(), product.getName(), variantDTOs,product.getImgUrl());
        }).collect(Collectors.toList());
    }
    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }
    public void saveProduct(Product product) {
        productRepository.save(product);
    }



    @Transactional
    public void deleteProduct(int productId) {
        variantRepository.deleteByProductId(productId); // Xóa tất cả variants trước
        productRepository.deleteById(productId);        // Xóa product
    }
}
