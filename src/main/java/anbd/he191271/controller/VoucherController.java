package anbd.he191271.controller;

import anbd.he191271.entity.Voucher;
import anbd.he191271.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    // ========== MANAGER ==========
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateVoucher(@PathVariable Long id) {
        voucherService.deactivateVoucher(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateVoucher(@PathVariable Long id) {
        voucherService.activateVoucher(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<List<Voucher>> searchVouchers(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean percent,
            @RequestParam(required = false) Boolean active
    ) {
        List<Voucher> results = voucherService.searchVouchers(code, percent, active);
        return ResponseEntity.ok(results);
    }

    // ========== CUSTOMER ==========
    @GetMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @RequestParam String code,
            @RequestParam double total
    ) {
        double newTotal = voucherService.applyVoucher(code, total);
        return ResponseEntity.ok(Map.of(
                "originalTotal", total,
                "discountedTotal", newTotal,
                "discountAmount", total - newTotal
        ));
    }
}