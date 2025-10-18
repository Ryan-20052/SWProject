package anbd.he191271.controller;

import anbd.he191271.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/license")
public class LicenseController {
    private final LicenseService licenseService;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String plainKey = body.get("key");
        String email = body.get("email");
        if (plainKey == null || plainKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Key rỗng"));
        }
        LicenseService.VerificationResult r = licenseService.verifyPlainKeyForCustomer(plainKey, email);
        if (!r.isOk()) {
            return ResponseEntity.ok(Map.of("ok", false, "message", r.getMessage()));
        } else {
            String expired = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getExpiredAt());
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Key hợp lệ",
                    "expiredAt", expired,
                    "customerName", r.getCustomerName(),
                    "productName", r.getProductName()
            ));
        }
    }
}