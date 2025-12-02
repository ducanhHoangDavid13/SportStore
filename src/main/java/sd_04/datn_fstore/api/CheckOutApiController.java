package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutApiController {

    private final CheckoutService checkoutService;
    private final VnPayService vnPayService;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final PhieuGiamgiaService phieuGiamgiaService;

    // =========================================================================
    // 1. API TÍNH TOÁN TỔNG TIỀN (SỬA LỖI BIGDECIMAL VS DOUBLE)
    // =========================================================================
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateOrder(@RequestBody CheckoutRequest request) {
        // 1. Tính tổng tiền hàng (SubTotal)
        double subTotal = 0.0; // Dùng double nguyên thủy cho dễ tính toán

        if (request.getItems() != null) {
            for (CheckoutRequest.CartItem item : request.getItems()) {
                SanPhamChiTiet sp = sanPhamCTRepository.findById(item.getSanPhamChiTietId()).orElse(null);

                // --- SỬA LỖI Ở ĐÂY ---
                if (sp != null && sp.getGiaTien() != null) {
                    // Chuyển BigDecimal sang double để nhân
                    double donGia = sp.getGiaTien().doubleValue();
                    subTotal += donGia * item.getSoLuong();
                }
            }
        }

        Double discountAmount = 0.0;
        String voucherMessage = "";
        boolean voucherValid = false;

        // 2. Logic Voucher (Gọi Service)
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            PhieuGiamgiaService.VoucherCheckResult result =
                    phieuGiamgiaService.kiemTraVoucherHople(request.getVoucherCode(), subTotal);

            if (result.isValid()) {
                discountAmount = result.discountAmount();
                voucherValid = true;
                voucherMessage = result.message();
            } else {
                voucherMessage = result.message();
            }
        }

        // 3. Xử lý Phí Ship (SỬA LỖI ÉP KIỂU)
        double shipFee = 0.0;
        if (request.getShippingFee() != null) {
            // Chuyển BigDecimal sang double
            shipFee = request.getShippingFee().doubleValue();
        }

        // 4. Tính tổng cuối
        double finalTotal = Math.max(0, subTotal + shipFee - discountAmount);

        // 5. Trả về kết quả
        Map<String, Object> response = new HashMap<>();
        response.put("subTotal", subTotal);
        response.put("discountAmount", discountAmount);
        response.put("finalTotal", finalTotal);
        response.put("voucherValid", voucherValid);
        response.put("voucherMessage", voucherMessage);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 2. CÁC API KHÁC GIỮ NGUYÊN
    // =========================================================================
    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody CheckoutRequest request, HttpServletRequest httpReq) {
        try {
            String clientIp = getClientIp(httpReq);
            CheckoutResponse response = checkoutService.placeOrder(request, clientIp);
            return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new CheckoutResponse(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @GetMapping("/vnpay-return")
    public void handleVnPayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
        String secureHash = request.getParameter("vnp_SecureHash");
        if (secureHash != null) vnpParams.put("vnp_SecureHash", secureHash);

        try {
            int result = vnPayService.orderReturn(vnpParams);
            String orderCode = vnpParams.get("vnp_TxnRef");
            if (result == 1) response.sendRedirect("/checkout/success?id=" + orderCode);
            else response.sendRedirect("/checkout?error=payment_failed&code=" + orderCode);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/checkout?error=system_error");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}