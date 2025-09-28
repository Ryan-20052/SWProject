package anbd.he191271.controller;

import anbd.he191271.entity.Product;
import anbd.he191271.entity.Variant;
import anbd.he191271.service.VariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class ManagerController {
    private final VariantService variantService;

    public ManagerController(VariantService variantService) {
        this.variantService = variantService;
    }
@GetMapping("/loadVariant")
    public List<Variant> LoadVariant(){
        return variantService.getAllVariant();
    }
    @PostMapping("/editVariant")
    public Void  editVariant(@RequestBody Variant variant){

    }


}
