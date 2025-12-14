package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Bổ sung
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.*; // Giả định chứa các DTO CalculateTotalRequest, CheckoutRequest, v.v.
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
    private final KhachhangService khachhangService;

    // Định nghĩa phí ship cố định ở server
    private static final BigDecimal FIXED_SHIPPING_FEE = new BigDecimal("30000");

    // --- HELPER: Lấy ID khách hàng đã đăng nhập ---
    private Integer getLoggedInCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        KhachHang khachHang = khachhangService.findByEmail(username); // Giả định KhachhangService có findByEmail

        if (khachHang == null) {
            // Có thể ném ngoại lệ nếu cần, nhưng trả về null cũng là một lựa chọn nếu bạn cho phép checkout ẩn danh
            throw new UsernameNotFoundException("Không tìm thấy khách hàng cho tài khoản đã đăng nhập: " + username);
        }

        return khachHang.getId();
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
            response.put("subTotal", BigDecimal.ZERO);
            response.put("shippingFee", FIXED_SHIPPING_FEE);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("finalTotal", FIXED_SHIPPING_FEE);
            response.put("voucherValid", false);
            response.put("voucherMessage", "Giỏ hàng trống.");
            return ResponseEntity.ok(response);
        }

        // Tạo DTO cho service tính toán
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
                // Trả về OK để frontend loại bỏ sản phẩm không tồn tại khỏi giỏ hàng
                response.put("error", "Sản phẩm không tồn tại (ID: " + item.getSanPhamChiTietId() + ") và đã được loại bỏ.");
                response.put("outOfStock", true);
                response.put("productId", item.getSanPhamChiTietId());
                response.put("recheck", true);
                return ResponseEntity.ok(response);
            }

            SanPhamChiTiet sp = spOpt.get();

            // Check tồn kho
            if (sp.getSoLuong() < item.getSoLuong()) {
                response.put("error", "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' không đủ hàng (Còn: " + sp.getSoLuong() + ")");
                response.put("outOfStock", true);
                response.put("productId", sp.getId());
                response.put("recheck", true);
                return ResponseEntity.ok(response); // Trả về OK để Frontend xử lý thông báo và điều chỉnh số lượng/xóa
            }

            // Lấy giá chuẩn từ SPCT
            BigDecimal donGia = sp.getGiaTien() != null ? sp.getGiaTien() : BigDecimal.ZERO;
            subTotal = subTotal.add(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));

            // Gán lại cho DTO CalculateTotalRequest
            CalculateTotalRequest.CartItem calcItem = new CalculateTotalRequest.CartItem();
            calcItem.setSanPhamChiTietId(item.getSanPhamChiTietId());
            calcItem.setSoLuong(item.getSoLuong());
            calcItem.setDonGia(donGia); // Gán đơn giá lấy từ DB
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

            // 2. Gắn ID khách hàng đã đăng nhập (Không cần kiểm tra null ở đây, vì nếu null sẽ là đơn hàng khách vãng lai)
            Integer loggedInCustomerId = getLoggedInCustomerId();

            // Nếu loggedInCustomerId là null, service cần xử lý đây là đơn hàng khách vãng lai (guest)
            // Nếu bạn bắt buộc phải đăng nhập, hãy ném lỗi tại đây:
            // if (loggedInCustomerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập để đặt hàng."));

            request.setKhachHangId(loggedInCustomerId);

            // ⚠️ Quan trọng: AddressId phải được gửi từ Frontend và được Service sử dụng
            // Đảm bảo CheckoutRequest có getAddressId() và setAddressId()
            if (request.getAddressId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng chọn địa chỉ giao hàng."));
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
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Tài khoản đăng nhập không hợp lệ."));
        } catch (RuntimeException e) {
            // Bắt các lỗi RuntimeException từ Service (như hết hàng, voucher lỗi, địa chỉ lỗi)
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