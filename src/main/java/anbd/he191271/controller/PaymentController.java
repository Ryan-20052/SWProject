package anbd.he191271.controller;

import anbd.he191271.config.VNPAYConfig;
import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;
import anbd.he191271.service.PaymentService;
import anbd.he191271.util.VNPAYUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        if ("00".equals(result.getCode())) {
            response.sendRedirect("http://localhost:8080/result.html?status=success&orderId=" + params.get("vnp_TxnRef"));
        } else {
            response.sendRedirect("http://localhost:8080/result.html?status=fail&code=" + result.getCode());
        }
    }

    @GetMapping("/vnpay-ipn")
    public PaymentResponseDTO paymentIpn(@RequestParam Map<String, String> params) throws Exception {
        return paymentService.handleIpn(params);
    }
}