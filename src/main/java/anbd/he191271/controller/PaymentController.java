package anbd.he191271.controller;

import anbd.he191271.config.VNPAYConfig;
import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;
import anbd.he191271.service.PaymentService;
import anbd.he191271.util.VNPAYUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO requestDTO,
                                            HttpServletRequest request) throws Exception {
        String clientIp = request.getRemoteAddr();
        return paymentService.createPayment(requestDTO, clientIp);
    }

    @GetMapping("/vnpay-return")
    public void paymentReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        PaymentResponseDTO result = paymentService.handleReturn(params);

        // ✅ Nếu thanh toán thành công, update luôn trạng thái Order tại đây (dành cho môi trường local/test)
        if ("00".equals(result.getCode()) && result.getOrderCode() != null) {
            paymentService.updateOrderStatus(result.getOrderCode(), "PAID");
        } else if (result.getOrderCode() != null) {
            paymentService.updateOrderStatus(result.getOrderCode(), "FAILED");
        }
        // 🔥 Redirect kèm thêm dữ liệu từ DTO
        String redirectUrl = "http://localhost:8080/result.html"
                + "?status=" + ("00".equals(result.getCode()) ? "success" : "fail")
                + "&code=" + result.getCode()
                + "&orderId=" + (result.getOrderId() != null ? result.getOrderId() : "")
                + "&orderCode=" + (result.getOrderCode() != null ? result.getOrderCode() : "")
                + "&totalAmount=" + (result.getTotalAmount() != null ? result.getTotalAmount() : "")
                + "&customerName=" + URLEncoder.encode(
                result.getCustomerName() != null ? result.getCustomerName() : "", StandardCharsets.UTF_8)
                + "&customerEmail=" + URLEncoder.encode(
                result.getCustomerEmail() != null ? result.getCustomerEmail() : "", StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    @GetMapping(value = "/vnpay-ipn", produces = "application/json")
    public ResponseEntity<Map<String,String>> paymentIpn(@RequestParam Map<String, String> params) throws Exception {
        PaymentResponseDTO dto = paymentService.handleIpn(params);
        Map<String,String> body = new HashMap<>();
        body.put("RspCode", dto.getCode());
        body.put("Message", dto.getMessage());
        return ResponseEntity.ok(body);
    }
}