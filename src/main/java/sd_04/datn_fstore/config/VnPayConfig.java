package sd_04.datn_fstore.config;

import jakarta.servlet.http.HttpServletRequest; // <-- PHẢI THÊM IMPORT NÀY
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class VnPayConfig {

    // ----- SỬA LỖI 1 & 2: Đổi tên biến để ServiceImpl có thể thấy -----

    // Website ID (Mã website)
    public static String vnp_TmnCode = "KW1AXRI5"; // (Đã giữ)

    // Secret Key (Chuỗi bí mật)
    public static String secretKey = "9D142OAUUNL49CEFPR8RZD6X0N5KLJE2"; // (Đã sửa tên)

    // VNPAY URL
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // (Đã sửa tên)

    // URL VNPAY gọi về (IPN/Return)
    public static String vnp_ReturnUrl = "http://localhost:8080/vnpay/payment-result-view";


    // ----- SỬA LỖI 3: Thêm lại hàm getIpAddress đã bị xóa -----

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP:" + e.getMessage();
        }
        return ipAddress;
    }

    // (Hàm hmacSHA512 của bạn đã OK, giữ nguyên)
    public static String hmacSHA512(final String key, final String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] resultBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * resultBytes.length);
            for (byte b : resultBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi HmacSHA512", e);
        }
    }

    // (Hàm getRandomNumber của bạn, giữ nguyên)
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}