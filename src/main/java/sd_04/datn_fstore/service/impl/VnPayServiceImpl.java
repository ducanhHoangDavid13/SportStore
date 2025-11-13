package sd_04.datn_fstore.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.config.VnPayConfig;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService; // Import BanHangService
import sd_04.datn_fstore.service.VnPayService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    // Inject BanHangService để lưu hóa đơn tạm trước
    private final BanHangService banHangService;

    @Override
    public String createOrder(CreateOrderRequest request, HttpServletRequest httpReq)
            throws UnsupportedEncodingException {

        // 1. Sửa: Gọi hàm luuHoaDonTam() để tạo HĐ (trạng thái 5) trước
        // Điều này đảm bảo HĐ có trong DB trước khi gửi sang VNPAY
        HoaDon savedHoaDon = banHangService.luuHoaDonTam(request);

        // 2. Dùng Mã HĐ thật từ DB làm mã giao dịch
        String vnp_TxnRef = savedHoaDon.getMaHoaDon();

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang " + vnp_TxnRef;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = VnPayConfig.vnp_ReturnUrl;

        // Lấy tổng tiền (đã trừ voucher) và nhân 100
        long amount = (long) (request.getTotalAmount() * 100);

        String vnp_IpAddr = httpReq.getRemoteAddr();
        if (vnp_IpAddr == null || vnp_IpAddr.isEmpty()) {
            vnp_IpAddr = "127.0.0.1";
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef); // Dùng Mã HĐ thật
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 3. Tạo chữ ký
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return VnPayConfig.vnp_Url + "?" + query.toString();
    }

    @Override
    public int orderReturn(Map<String, String> vnpParams) {
        // (Logic xử lý IPN / Return Url - Giữ nguyên)
        // ...
        return 0;
    }
}