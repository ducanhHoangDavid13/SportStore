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
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.HoaDonChiTietRepository;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import sd_04.datn_fstore.repository.SanPhamCTRepository; // <-- THÊM
import sd_04.datn_fstore.service.SanPhamService;        // <-- THÊM
import sd_04.datn_fstore.service.PhieuGiamgiaService;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    // Use ObjectProvider to lazily obtain BanHangService when needed (breaks constructor-time cycle)
    private final ObjectProvider<BanHangService> banHangServiceProvider;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final SanPhamService sanPhamService;
    private final PhieuGiamgiaService phieuGiamgiaService;
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

        // 🚀 THÊM Mã BankCode (NCB)
        String vnp_BankCode = "NCB";

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

        // 🚀 THÊM tham số BankCode
        vnp_Params.put("vnp_BankCode", vnp_BankCode);

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

    // ... (Các import giữ nguyên)

    // ... (Hàm createOrder giữ nguyên) ...

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int orderReturn(Map<String, String> vnpParams) {
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");

        try {
            // 1. Kiểm tra chữ ký bảo mật
            if (!validateHash(vnpParams)) {
                return -1;
            }

            // 2. Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn: " + vnp_TxnRef));

            // 3. Kiểm tra trạng thái để tránh xử lý lặp lại
            // Chỉ xử lý nếu đơn đang "Chờ thanh toán" (6)
            if (hoaDon.getTrangThai() != 6) {
                return (hoaDon.getTrangThai() == 1) ? 1 : 0;
            }

            // =================================================================
            // TRƯỜNG HỢP 1: THANH TOÁN THÀNH CÔNG (Code == "00")
            // =================================================================
            if ("00".equals(vnp_ResponseCode)) {
                // Đã trừ kho lúc đặt hàng rồi -> Chỉ cần update trạng thái đơn
                hoaDon.setTrangThai(1); // 1: Đã xác nhận/Chờ đóng gói
                hoaDon.setNgayTao(java.time.LocalDateTime.now());
                hoaDon.setHinhThucThanhToan(2); // VNPAY
                hoaDonRepository.save(hoaDon);
                return 1;
            }

            // =================================================================
            // TRƯỜNG HỢP 2: KHÁCH HỦY / THẤT BẠI (Code != "00")
            // =================================================================
            else {
                // 1. Đổi trạng thái đơn sang Hủy (5)
                hoaDon.setTrangThai(5);
                hoaDonRepository.save(hoaDon);

                // 2. ▼▼▼ LOGIC HOÀN LẠI KHO (CỰC KỲ QUAN TRỌNG) ▼▼▼
                List<HoaDonChiTiet> listItems = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

                for (HoaDonChiTiet item : listItems) {
                    SanPhamChiTiet spct = item.getSanPhamChiTiet();

                    // Lấy tồn kho cũ + Số lượng khách đã đặt nhưng hủy
                    int soLuongMoi = spct.getSoLuong() + item.getSoLuong();
                    spct.setSoLuong(soLuongMoi);

                    // Nếu sản phẩm đang bị ẩn (trạng thái 0) do hết hàng, giờ có hàng lại thì MỞ BÁN LẠI (1)
                    if (spct.getTrangThai() == 0 && soLuongMoi > 0) {
                        spct.setTrangThai(1);
                    }

                    sanPhamCTRepository.save(spct); // Lưu lại vào DB
                }

                // 3. Cập nhật lại tổng số lượng cho sản phẩm cha (Optional nhưng nên có)
                if (!listItems.isEmpty()) {
                    sanPhamService.updateTotalQuantity(listItems.get(0).getSanPhamChiTiet().getSanPham().getId());
                }

                System.out.println("Đã hoàn kho cho đơn hủy: " + vnp_TxnRef);
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean validateHash(Map<String, String> vnpParams) {
        String receivedHash = vnpParams.get("vnp_SecureHash");

        Map<String, String> fields = new HashMap<>(vnpParams);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String key : fieldNames) {
            String value = fields.get(key);
            if (value != null && value.length() > 0) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(key);
                hashData.append('=');
                hashData.append(value);
            }
        }

        String generatedHash = VnPayConfig.hmacSHA512(
                VnPayConfig.secretKey,
                hashData.toString()
        );

        return generatedHash.equals(receivedHash);
    }
}