package anbd.he191271.service;

import anbd.he191271.entity.Variant;
import anbd.he191271.repository.VariantRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VariantService {
    private final VariantRepository repo;

    public VariantService(VariantRepository repo) {
        this.repo = repo;
    }

    public List<Variant> getVariantsByProduct(int productId) {
        return repo.findByProductId(productId);
    }
    public List<Variant> getAllVariant() {
        return repo.findAll();
    }
    public Variant getVariantById(int id) {
        return repo.findById(id).get();
    }
    public void deleteVariant(int id) {
        repo.deleteById(id);
    }
    public Variant create(Variant variant) {
        return repo.save(variant);
    }
}
