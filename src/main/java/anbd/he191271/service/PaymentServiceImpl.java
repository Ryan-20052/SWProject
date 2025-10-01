package anbd.he191271.service;

import anbd.he191271.config.VNPAYConfig;
import anbd.he191271.dto.PaymentRequestDTO;
import anbd.he191271.dto.PaymentResponseDTO;
import anbd.he191271.util.VNPAYUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private VNPAYConfig vnpConfig;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO, String clientIp) throws Exception {
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(requestDTO.getAmount() * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", requestDTO.getOrderInfo());
        vnp_Params.put("vnp_OrderType", requestDTO.getOrderType() != null ? requestDTO.getOrderType() : "other");
        vnp_Params.put("vnp_Locale", requestDTO.getLocale() != null ? requestDTO.getLocale() : "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", clientIp);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String paymentUrl = VNPAYUtil.getPaymentURL(vnp_Params, vnpConfig.getPayUrl(), vnpConfig.getHashSecret());

        return new PaymentResponseDTO("00", "Success", paymentUrl);
    }

    @Override
    public PaymentResponseDTO handleReturn(Map<String, String> params) throws Exception {
        boolean check = VNPAYUtil.validateSignature(params, vnpConfig.getHashSecret());
        if (check) {
            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                return new PaymentResponseDTO("00", "Thanh toán thành công", null);
            } else {
                return new PaymentResponseDTO(responseCode, "Thanh toán thất bại", null);
            }
        } else {
            return new PaymentResponseDTO("97", "Sai chữ ký", null);
        }
    }

    @Override
    public PaymentResponseDTO handleIpn(Map<String, String> params) throws Exception {
        boolean check = VNPAYUtil.validateSignature(params, vnpConfig.getHashSecret());
        if (check) {
            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                // TODO: cập nhật trạng thái đơn hàng trong DB
                return new PaymentResponseDTO("00", "IPN thành công", null);
            } else {
                return new PaymentResponseDTO(responseCode, "IPN thất bại", null);
            }
        } else {
            return new PaymentResponseDTO("97", "Sai chữ ký IPN", null);
        }
    }
}