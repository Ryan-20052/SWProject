package anbd.he191271.service;

import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;

import java.util.Map;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO, String clientIp) throws Exception;
    PaymentResponseDTO handleReturn(Map<String, String> params) throws Exception;
    PaymentResponseDTO handleIpn(Map<String, String> params) throws Exception;
}
