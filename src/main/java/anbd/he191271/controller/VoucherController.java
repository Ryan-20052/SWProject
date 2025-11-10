package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.entity.ManagerLog;
import anbd.he191271.entity.Voucher;
import anbd.he191271.service.ManagerLogService;
import anbd.he191271.service.VoucherService;
import jakarta.servlet.http.HttpSession;
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
    @Autowired
    private ManagerLogService managerLogService;

    // ========== MANAGER ==========
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(voucherService.getAllVouchersPaged(page, size));
    }


    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        ManagerLog log =  new ManagerLog(manager.getUsername(), "add voucher "+ voucher.getCode());
        managerLogService.save(log);
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        ManagerLog log =  new ManagerLog(manager.getUsername(), "Update information voucher id: " + id );
        managerLogService.save(log);
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateVoucher(@PathVariable Long id, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        ManagerLog log =  new ManagerLog(manager.getUsername(), "deactivate voucher id: " + id );
        managerLogService.save(log);
        voucherService.deactivateVoucher(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateVoucher(@PathVariable Long id, HttpSession session) {
        Manager manager=(Manager)session.getAttribute("manager");
        ManagerLog log =  new ManagerLog(manager.getUsername(), "activate voucher id: " + id );
        managerLogService.save(log);
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
        try {
            double newTotal = voucherService.applyVoucher(code, total);
            double discountAmount = total - newTotal;
            boolean reachedMinimum = newTotal <= 5000.0;

            var voucherOpt = voucherService.findByCode(code);
            Double maxDiscount = voucherOpt.map(v -> v.getMaxDiscountAmount()).orElse(null);
            Double minOrderAmount = voucherOpt.map(v -> v.getMinOrderAmount()).orElse(0.0);

            return ResponseEntity.ok(Map.of(
                    "originalTotal", total,
                    "discountedTotal", newTotal,
                    "discountAmount", discountAmount,
                    "reachedMinimum", reachedMinimum,
                    "minimumAmount", 5000.0,
                    "maxDiscountAmount", maxDiscount,
                    "minOrderAmount", minOrderAmount,
                    "voucherCode", code
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{code}/use")
    public ResponseEntity<Map<String, Object>> useVoucher(@PathVariable String code) {
        try {
            voucherService.decreaseUsage(code); // 🔥 Gọi lại method gốc
            return ResponseEntity.ok(Map.of(
                    "message", "Voucher đã được sử dụng thành công!",
                    "voucherCode", code
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}