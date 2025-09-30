package anbd.he191271.controller;

import anbd.he191271.entity.Variant;
import anbd.he191271.service.VariantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variant")
public class VariantController {
    private final VariantService service;

    public VariantController(VariantService service) {
        this.service = service;
    }

    // Lấy variant theo product
    @GetMapping("/product/{productId}")
    public List<Variant> getByProduct(@PathVariable int productId) {
        return service.getVariantsByProduct(productId);
    }

    // Tạo variant mới
    @PostMapping
    public Variant create(@RequestBody Variant variant) {
        return service.create(variant);
    }
}
