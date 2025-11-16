package sd_04.datn_fstore.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.config.VnPayConfig;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VnPay service implementation.
 *
 * Note: To break the circular dependency between BanHangServiceImpl <-> VnPayServiceImpl
 * we inject a lazy provider for BanHangService (ObjectProvider<BanHangService>).
 * This defers obtaining the BanHangService bean until it's actually needed inside the callback,
 * avoiding constructor-time circular instantiation.
 */
@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    // Use ObjectProvider to lazily obtain BanHangService when needed (breaks constructor-time cycle)
    private final ObjectProvider<BanHangService> banHangServiceProvider;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    @Override
    public String createOrder(long amount, String orderInfo, String orderCode, String ipAddress)
            throws UnsupportedEncodingException {

        String vnp_TxnRef = orderCode;
        long amountToVnPay = amount;
        String vnp_IpAddr = ipAddress;
        String vnp_OrderInfo = orderInfo;

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = VnPayConfig.vnp_ReturnUrl;
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountToVnPay));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
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

        // Build hashData and query string (sorted by key)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                hashData.append(fieldName).append('=').append(encodedValue);
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                        .append('=')
                        .append(encodedValue);
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
        String vnp_SecureHashReceived = vnpParams.get("vnp_SecureHash");

        try {
            // 1) Validate secure hash
            Map<String, String> paramsForHash = new HashMap<>(vnpParams);
            paramsForHash.remove("vnp_SecureHash");
            paramsForHash.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(paramsForHash.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = paramsForHash.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                    hashData.append(fieldName).append('=').append(encodedValue);
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String localHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
            if (vnp_SecureHashReceived == null || !localHash.equalsIgnoreCase(vnp_SecureHashReceived)) {
                // Invalid signature
                System.err.println("VNPAY secure hash mismatch. received=" + vnp_SecureHashReceived + " computed=" + localHash);
                return -1;
            }

            // 2) Find invoice
            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn: " + vnp_TxnRef));

            // If already paid
            if (hoaDon.getTrangThai() == 1) {
                return 1;
            }

            // Only process when current status is the "temporary/pending" status expected (5 in your code)
            if (hoaDon.getTrangThai() != 5) {
                return -1;
            }

            if ("00".equals(vnp_ResponseCode)) {
                List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
                if (items == null || items.isEmpty()) {
                    throw new RuntimeException("Hóa đơn không có sản phẩm chi tiết.");
                }

                // Build DTO list for inventory decrement
                List<CreateOrderRequest.Item> itemsToDecrement = new ArrayList<>();
                for (HoaDonChiTiet hdct : items) {
                    itemsToDecrement.add(new CreateOrderRequest.Item(
                            hdct.getSanPhamChiTiet().getId(),
                            hdct.getSoLuong(),
                            hdct.getDonGia()
                    ));
                }

                // Lazily obtain BanHangService to avoid circular dependency at context startup
                BanHangService banHangService = banHangServiceProvider.getIfAvailable();
                if (banHangService == null) {
                    throw new RuntimeException("BanHangService is not available");
                }

                // Decrement inventory and voucher via BanHangService
                banHangService.decrementInventory(itemsToDecrement);
                banHangService.decrementVoucher(hoaDon.getPhieuGiamGia());

                hoaDon.setTrangThai(1); // 1 = Đã thanh toán
                hoaDonRepository.save(hoaDon);

                return 1; // Thành công

            } else {
                // Payment failed
                hoaDon.setTrangThai(3); // 3 = Đã hủy
                hoaDonRepository.save(hoaDon);
                return 0; // Thất bại
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Lỗi
        }
    }
}