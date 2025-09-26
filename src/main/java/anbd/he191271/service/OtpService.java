package anbd.he191271.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // Map để lưu OTP tạm thời: email -> OTP info
    private final Map<String, OtpInfo> otpStorage = new ConcurrentHashMap<>();

    // Tạo OTP mới
    public String generateOtp(String email) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000); // 6 số
        otpStorage.put(email, new OtpInfo(otp, LocalDateTime.now().plusMinutes(5))); // Hết hạn sau 5 phút
        return otp;
    }

    // Kiểm tra OTP có hợp lệ không (không xóa ngay)
    public boolean validateOtp(String email, String otp) {
        OtpInfo info = otpStorage.get(email);

        if (info == null) {
            return false; // Không có OTP cho email này
        }

        if (info.expiry.isBefore(LocalDateTime.now())) {
            otpStorage.remove(email); // OTP hết hạn thì xóa
            return false;
        }

        return info.otp.equals(otp);
    }

    // Xoá OTP sau khi reset mật khẩu thành công
    public void clearOtp(String email) {
        otpStorage.remove(email);
    }

    // Lớp chứa OTP và thời gian hết hạn
    private record OtpInfo(String otp, LocalDateTime expiry) {}
}