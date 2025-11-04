package anbd.he191271.service;

import anbd.he191271.entity.Variant;
import anbd.he191271.repository.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariantService {

    private final VariantRepository variantRepository;

    public VariantService(VariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    // Lấy tất cả variant theo productId
    public List<Variant> findByProductId(int productId) {
        return variantRepository.findByProductId(productId);
    }

    // Lấy variant theo id
    public Variant findById(int id) {
        return variantRepository.findById(id).orElse(null);
    }

    public List<Variant> getVariantsByProduct(int productId) {
        return variantRepository.findByProductId(productId);
    }
    public List<Variant> getAllVariant() {
        return variantRepository.findAll();
    }
    public Variant getVariantById(int id) {
        return variantRepository.findById(id).get();
    }
    public void deleteVariant(int id) {
        variantRepository.deleteById(id);
    }
    public void  saveVariant(Variant variant) {
        variantRepository.save(variant);
    }
    public Variant create(Variant variant) {
        return variantRepository.save(variant);
    }
    public void  saveAll(List<Variant> variants) {
        variantRepository.saveAll(variants);
    }
}
