package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.KhachhangService;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutApiController {

    private final CheckoutService checkoutService;
    private final VnPayService vnPayService;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final PhieuGiamgiaService phieuGiamgiaService;

    // BỔ SUNG: Dịch vụ Khách hàng để lấy ID
    private final KhachhangService khachhangService;

    // Định nghĩa phí ship cố định ở server
    private static final BigDecimal FIXED_SHIPPING_FEE = new BigDecimal("30000");

    // --- HELPER: Lấy ID khách hàng đã đăng nhập ---
    private Integer getLoggedInCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Trả về null nếu chưa đăng nhập hoặc là người dùng ẩn danh
        }

        // Giả định tên người dùng là Email
        String username = authentication.getName();
        // Giả định KhachhangService có hàm findByEmail(String)
        KhachHang khachHang = khachhangService.findByEmail(username);

        // Trả về ID nếu tìm thấy khách hàng
        return khachHang != null ? khachHang.getId() : null;
    }
    // ----------------------------------------------------

    /**
     * API TÍNH TOÁN LẠI TOÀN BỘ GIÁ TRỊ ĐƠN HÀNG MỘT CÁCH AN TOÀN
     * Endpoint: POST /api/checkout/calculate
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateOrder(@RequestBody CheckoutRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;
        Map<String, Object> response = new HashMap<>();

        // 1. KIỂM TRA GIỎ HÀNG CÓ RỖNG KHÔNG
        if (request.getItems() == null || request.getItems().isEmpty()) {
            // Trả về OK với total = 0 nếu Frontend gọi calculate khi giỏ hàng trống
            response.put("subTotal", BigDecimal.ZERO);
            response.put("shippingFee", FIXED_SHIPPING_FEE);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("finalTotal", FIXED_SHIPPING_FEE);
            response.put("voucherValid", false);
            response.put("voucherMessage", "Giỏ hàng trống.");
            return ResponseEntity.ok(response);
        }

        // Dùng DTO mới cho calculate
        CalculateTotalRequest calcRequest = new CalculateTotalRequest();
        calcRequest.setVoucherCode(request.getVoucherCode());
        calcRequest.setShippingFee(FIXED_SHIPPING_FEE);

        List<CalculateTotalRequest.CartItem> calcItems = new java.util.ArrayList<>();

        // 2. TÍNH TỔNG TIỀN HÀNG (SUB-TOTAL) TỪ DATABASE VÀ CHECK TỒN KHO
        for (CheckoutRequest.CartItem item : request.getItems()) {
            if (item.getSanPhamChiTietId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Dữ liệu giỏ hàng lỗi: thiếu ID sản phẩm."));
            }

            Optional<SanPhamChiTiet> spOpt = sanPhamCTRepository.findById(item.getSanPhamChiTietId());

            if (spOpt.isEmpty()) {
                response.put("error", "Sản phẩm không tồn tại (ID: " + item.getSanPhamChiTietId() + ")");
                return ResponseEntity.badRequest().body(response);
            }

            SanPhamChiTiet sp = spOpt.get();

            // Check tồn kho
            if (sp.getSoLuong() < item.getSoLuong()) {
                response.put("error", "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' không đủ hàng (Còn: " + sp.getSoLuong() + ")");
                response.put("outOfStock", true);
                response.put("productId", sp.getId());
                return ResponseEntity.ok(response); // Trả về OK để Frontend xử lý thông báo và xóa khỏi giỏ
            }

            // Lấy giá chuẩn từ SPCT
            BigDecimal donGia = sp.getGiaTien() != null ? sp.getGiaTien() : BigDecimal.ZERO;

            subTotal = subTotal.add(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));

            // Gán lại cho DTO CalculateTotalRequest (quan trọng để Service tính toán)
            CalculateTotalRequest.CartItem calcItem = new CalculateTotalRequest.CartItem();
            calcItem.setSanPhamChiTietId(item.getSanPhamChiTietId());
            calcItem.setSoLuong(item.getSoLuong());
            calcItem.setDonGia(donGia);
            calcItems.add(calcItem);
        }
        calcRequest.setItems(calcItems);

        // 3. GỌI SERVICE TÍNH TOÁN TỔNG THỂ VÀ VOUCHER
        CalculateTotalResponse calcRes = checkoutService.calculateOrderTotal(calcRequest);

        // 4. TRẢ VỀ KẾT QUẢ
        response.put("subTotal", calcRes.getSubTotal());
        response.put("shippingFee", FIXED_SHIPPING_FEE);
        response.put("discountAmount", calcRes.getDiscountAmount());
        response.put("finalTotal", calcRes.getFinalTotal());
        response.put("voucherValid", calcRes.isVoucherValid());
        response.put("voucherMessage", calcRes.getVoucherMessage());
        // Trả về mã đã áp dụng nếu hợp lệ
        response.put("appliedVoucherCode", calcRes.isVoucherValid() ? request.getVoucherCode() : null);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ ĐẶT HÀNG & THANH TOÁN
    // =========================================================================

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody CheckoutRequest request, HttpServletRequest httpReq) {
        try {
            // 1. Ghi đè phí ship để bảo mật
            request.setShippingFee(FIXED_SHIPPING_FEE);

            // 2. BỔ SUNG: Gắn ID khách hàng đã đăng nhập (nếu có)
            Integer loggedInCustomerId = getLoggedInCustomerId();
            if (loggedInCustomerId != null) {
                // SỬA: Lỗi cannot find symbol được giải quyết khi DTO CheckoutRequest có setKhachHangId
                request.setKhachHangId(loggedInCustomerId);
            }

            String clientIp = getClientIp(httpReq);
            CheckoutResponse response = checkoutService.placeOrder(request, clientIp);

            // Map CheckoutResponse sang format Frontend mong muốn (success/message/redirectUrl)
            return ResponseEntity.ok(Map.of(
                    "success", response.isSuccess(),
                    "message", response.getMessage(),
                    "redirectUrl", response.getRedirectUrl(),
                    "paymentMethod", request.getPaymentMethod() // Trả lại payment method cho FE
            ));
        } catch (RuntimeException e) { // Bắt các lỗi RuntimeException từ Service (như hết hàng)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Đã có lỗi hệ thống xảy ra."));
        }
    }

    @GetMapping("/vnpay-return")
    public void handleVnPayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
        try {
            // Giả định orderReturn trả về 1 nếu thành công, 0 nếu thất bại
            int result = vnPayService.orderReturn(vnpParams);
            String orderCode = vnpParams.get("vnp_TxnRef");
            String redirectUrl = (result == 1)
                    ? "/checkout/success?orderCode=" + orderCode // Dùng orderCode để truy vấn đơn hàng
                    : "/checkout?error=payment_failed&code=" + orderCode;
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/checkout?error=system_error");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}