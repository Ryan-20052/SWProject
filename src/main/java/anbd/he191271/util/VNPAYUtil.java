package anbd.he191271.util;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class VNPAYUtil {

    private static final Logger log = LoggerFactory.getLogger(VNPAYUtil.class);
    public static String getPaymentURL(Map<String, String> params, String payUrl, String secretKey) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append('&');
            }
        }
        String queryUrl = query.toString();
        queryUrl = queryUrl.substring(0, queryUrl.length() - 1);

        String vnpSecureHash = hashAllFields(params, secretKey);
        return payUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
    }
    public static boolean validateSignature(Map<String, String> params, String secretKey) throws Exception {
        String receivedHash = params.get("vnp_SecureHash");

        // Bỏ chữ ký ra khỏi params để tính hash
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        // Tính chữ ký local
        String calculatedHash = hashAllFields(signParams, secretKey);

        // Ghi log ra console
        log.info("========== DEBUG VNPAY SIGNATURE ==========");
        log.info("HashData (chuỗi để ký): {}", buildHashData(signParams));
        log.info("Calculated Hash (Local): {}", calculatedHash);
        log.info("Received Hash (VNPAY):   {}", receivedHash);
        log.info("==========================================");

        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash);
    }

    private static String hashAllFields(Map<String, String> fields, String secretKey) throws Exception {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                sb.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    sb.append('&');
                }
            }
        }
        String hashData = sb.toString();
        return hmacSHA512(secretKey, hashData);
    }

    private static String buildHashData(Map<String, String> fields) throws Exception {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                sb.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    private static String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
        javax.crypto.spec.SecretKeySpec secretKeySpec =
                new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }
}