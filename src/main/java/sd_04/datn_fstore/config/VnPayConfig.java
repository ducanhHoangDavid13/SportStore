package sd_04.datn_fstore.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class VnPayConfig {

    // ĐÃ CẬP NHẬT TỪ ẢNH CỦA BẠN
    public static String vnp_TmnCode = "KW1AXRI5";
    public static String vnp_HashSecret = "RNVV4M5YFPQ2N40HAJPBNBN2EYWGN341";
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // URL VNPAY gọi về sau khi khách thanh toán
    // (BẠN PHẢI DÙNG NGROK ĐỂ TEST)
    public static String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/payment-result";

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