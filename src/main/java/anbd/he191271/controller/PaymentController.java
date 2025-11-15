package anbd.he191271.controller;

import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;
import anbd.he191271.service.PaymentService;
import anbd.he191271.service.PurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PurchaseService purchaseService;

    /**
     * ✅ 1. Tạo URL thanh toán VNPAY
     */
    @PostMapping("/create")
    public PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO requestDTO,
                                            HttpServletRequest request) throws Exception {
        String clientIp = request.getRemoteAddr();

        // ⚙️ Không cần tự tính totalAmount nữa, vì service đã làm việc đó rồi
        return paymentService.createPayment(requestDTO, clientIp);
    }

    /**
     * ✅ 2. VNPAY redirect về đây sau khi thanh toán xong (client side)
     */
    @GetMapping("/vnpay-return")
    public void paymentReturn(@RequestParam Map<String, String> params,
                              HttpServletResponse response,
                              HttpServletRequest request) throws Exception {
        PaymentResponseDTO result = paymentService.handleReturn(params);

        if (result != null && result.getOrderCode() != null) {
            boolean isSuccess = "00".equals(result.getCode());

            // ✅ Update trạng thái Order
            paymentService.updateOrderStatus(result.getOrderCode(), isSuccess ? "PAID" : "FAILED");

            // ✅ Nếu thanh toán thành công → tạo license key + gửi mail
            if (isSuccess) {
                try {
                    purchaseService.processSuccessfulPayment(result.getOrderCode());
                    System.out.println("✅ Đã gửi license key cho đơn hàng: " + result.getOrderCode());

                    // 🆕 THÊM THÔNG BÁO THÀNH CÔNG VÀO SESSION
                    HttpSession session = request.getSession();
                    session.setAttribute("paymentSuccess", true);
                    session.setAttribute("successOrderCode", result.getOrderCode());

                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi khi xử lý license key: " + e.getMessage());
                    HttpSession session = request.getSession();
                    session.setAttribute("paymentError", "Có lỗi xảy ra khi xử lý đơn hàng: " + e.getMessage());
                }
            } else {
                HttpSession session = request.getSession();
                session.setAttribute("paymentError", "Thanh toán thất bại");
            }

            // ✅ Redirect về trang homepage
            String redirectUrl = "http://localhost:8080/home/homepage";
            response.sendRedirect(redirectUrl);
        } else {
            // ❌ Nếu lỗi
            HttpSession session = request.getSession();
            session.setAttribute("paymentError", "Lỗi xử lý thanh toán");
            response.sendRedirect("http://localhost:8080/home/homepage");
        }
    }


    /**
     * ✅ 3. IPN callback chính thức từ VNPAY (server side)
     */
    @GetMapping(value = "/vnpay-ipn", produces = "application/json")
    public ResponseEntity<Map<String, String>> paymentIpn(@RequestParam Map<String, String> params) throws Exception {
        PaymentResponseDTO dto = paymentService.handleIpn(params);

        if (dto != null && "00".equals(dto.getCode()) && dto.getOrderCode() != null) {
            try {
                purchaseService.processSuccessfulPayment(dto.getOrderCode());
                System.out.println("✅ [IPN] License key đã được tạo cho order: " + dto.getOrderCode());
            } catch (Exception e) {
                System.err.println("⚠️ [IPN] Lỗi khi tạo license key: " + e.getMessage());
            }
        }

        Map<String, String> body = new HashMap<>();
        body.put("RspCode", dto != null ? dto.getCode() : "99");
        body.put("Message", dto != null ? dto.getMessage() : "Unknown error");

        return ResponseEntity.ok(body);
    }

    /**
     * 🔧 Hàm phụ để build URL redirect đẹp và tránh lỗi encode
     */
    // trong PaymentController - thay hàm buildRedirectUrl cũ bằng cái này:
    private String buildRedirectUrl(PaymentResponseDTO result) {
        // Nếu thành công -> show purchased licenses
        if (result != null && "00".equals(result.getCode())) {
            return "http://localhost:8080/purchasedlicenses?status=success&orderCode=" + encode(result.getOrderCode());
        }

        // Nếu thất bại -> redirect về purchasedlicenses với status fail (user có thể thử lại)
        String orderCodePart = (result != null && result.getOrderCode() != null)
                ? "&orderCode=" + encode(result.getOrderCode())
                : "";
        return "http://localhost:8080/purchasedlicenses?status=fail" + orderCodePart;
    }


    /**
     * 🔧 Encode chuỗi UTF-8 an toàn
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
