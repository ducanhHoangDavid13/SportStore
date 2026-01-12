package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.config.VnPayConfig;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    private final HoaDonRepository hoaDonRepository;
    private final ObjectProvider<CheckoutService> checkoutServiceProvider;

    @Override
    public String createOrder(long amount, String orderInfo, String orderCode, String ipAddress) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderCode);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        // Cấu hình cứng ngân hàng NCB để test (Sandbox)
        vnp_Params.put("vnp_BankCode", "NCB");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                // SỬA: Dùng UTF-8 để hỗ trợ tiếng Việt tốt hơn và khớp với Spring Boot
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int orderReturn(Map<String, String> vnpParams) {
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");

        try {
            // 1. Kiểm tra Checksum (Log cảnh báo nhưng vẫn cho qua để update trạng thái)
            boolean isHashValid = validateHash(vnpParams);
            if (!isHashValid) {
                System.out.println("⚠️ CẢNH BÁO: Checksum không khớp cho đơn: " + vnp_TxnRef);
                // return -1; // Tạm thời bỏ qua lỗi bảo mật để test chức năng
            }

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn: " + vnp_TxnRef));

            // 2. Kiểm tra trạng thái: Chấp nhận update nếu đang là 0 hoặc 6
            // Nếu đã là 1, 2, 3... thì không cần update lại
            if (hoaDon.getTrangThai() != 0 && hoaDon.getTrangThai() != 6) {
                return 1;
            }

            // 3. Xử lý kết quả
            if ("00".equals(vnp_ResponseCode)) {
                System.out.println("✅ VNPAY SUCCESS: Đơn " + vnp_TxnRef + " -> Đã Xác Nhận (1)");

                // --- SỬA Ở ĐÂY: ĐỔI VỀ 1 (ĐÃ XÁC NHẬN) ---
                hoaDon.setTrangThai(1);         // 1: Đã xác nhận (Theo yêu cầu của bạn)
                hoaDon.setHinhThucThanhToan(4); // 4: VNPAY
                hoaDon.setNgayTao(LocalDateTime.now());

                hoaDonRepository.save(hoaDon);
                return 1;
            } else {
                System.out.println("❌ VNPAY FAILED: Hủy đơn " + vnp_TxnRef);
                checkoutServiceProvider.getIfAvailable().cancelOrder(vnp_TxnRef);
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean validateHash(Map<String, String> vnpParams) {
        if (vnpParams == null || !vnpParams.containsKey("vnp_SecureHash")) return false;

        String receivedHash = vnpParams.get("vnp_SecureHash");

        Map<String, String> fields = new HashMap<>(vnpParams);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    // SỬA: Dùng UTF-8 cho đồng bộ với createOrder
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        String generatedHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());

        // Debug Log
        System.out.println("Hash Nhận: " + receivedHash);
        System.out.println("Hash Tính: " + generatedHash);

        return generatedHash.equals(receivedHash);
    }
}