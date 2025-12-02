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

    // Định nghĩa phí ship cố định ở server
    private static final BigDecimal FIXED_SHIPPING_FEE = new BigDecimal("30000");

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
            return ResponseEntity.badRequest().body("Giỏ hàng của bạn đang trống.");
        }

        // 2. TÍNH TỔNG TIỀN HÀNG (SUB-TOTAL) TỪ DATABASE
        for (CheckoutRequest.CartItem item : request.getItems()) {
            if (item.getSanPhamChiTietId() == null) {
                return ResponseEntity.badRequest().body("Dữ liệu giỏ hàng lỗi: thiếu ID sản phẩm.");
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
                return ResponseEntity.badRequest().body(response);
            }

            // --- QUAN TRỌNG: LOGIC ƯU TIÊN GIÁ KHUYẾN MÃI ---
            // Nếu có giá KM và giá KM > 0 thì lấy, ngược lại lấy giá gốc
            // Thay thế toàn bộ cụm logic tính donGia bị lỗi bằng dòng này:

            BigDecimal donGia = BigDecimal.ZERO;
            // SỬA: Luôn ưu tiên lấy giá của biến thể (SPCT) vì đó là món khách chọn mua
            if (sp.getGiaTien() != null) {
                donGia = sp.getGiaTien();
            } else if (sp.getSanPham() != null) {
                // Chỉ lấy giá cha nếu giá con bị null (trường hợp dự phòng)
                donGia = sp.getSanPham().getGiaTien();
            }

            subTotal = subTotal.add(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        // 3. XỬ LÝ MÃ GIẢM GIÁ (AUTO APPLY BEST VOUCHER)
        BigDecimal discountAmount = BigDecimal.ZERO;
        String voucherMessage = "";
        boolean voucherValid = false;

        // Lấy mã khách nhập (nếu có)
        String appliedCode = request.getVoucherCode();

        // LOGIC MỚI: Nếu khách KHÔNG nhập mã, hệ thống tự tìm mã tốt nhất
        if (appliedCode == null || appliedCode.trim().isEmpty()) {
            // Gọi service tìm mã tốt nhất (Bạn cần đảm bảo Service đã có hàm này như hướng dẫn trước)
            String bestCode = phieuGiamgiaService.timVoucherTotNhat(subTotal);
            if (bestCode != null) {
                appliedCode = bestCode;
                voucherMessage = "Đã tự động áp dụng mã tốt nhất: " + bestCode;
            }
        }

        // Kiểm tra tính hợp lệ của mã (Dù là khách nhập hay hệ thống tự tìm)
        if (appliedCode != null && !appliedCode.isEmpty()) {
            PhieuGiamgiaService.VoucherCheckResult result =
                    phieuGiamgiaService.kiemTraVoucherHople(appliedCode, subTotal);

            if (result.isValid()) {
                discountAmount = BigDecimal.valueOf(result.discountAmount()); // Convert double sang BigDecimal nếu DTO trả về double
                voucherValid = true;
                // Nếu khách tự nhập mã thì lấy thông báo từ kết quả check
                if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
                    voucherMessage = result.message();
                }
            } else {
                // Nếu mã tự tìm mà lại không hợp lệ (hiếm) thì reset
                if (request.getVoucherCode() == null || request.getVoucherCode().isEmpty()) {
                    appliedCode = null;
                    voucherMessage = "";
                } else {
                    voucherMessage = result.message(); // Báo lỗi nếu khách tự nhập sai
                }
            }
        }

        // 4. LẤY PHÍ VẬN CHUYỂN CỐ ĐỊNH
        BigDecimal shipFee = FIXED_SHIPPING_FEE;

        // 5. TÍNH TỔNG TIỀN CUỐI CÙNG
        BigDecimal finalTotal = subTotal.add(shipFee).subtract(discountAmount);
        finalTotal = finalTotal.max(BigDecimal.ZERO); // Không được âm

        // 6. TRẢ VỀ KẾT QUẢ
        response.put("subTotal", subTotal);
        response.put("shippingFee", shipFee);
        response.put("discountAmount", discountAmount);
        response.put("finalTotal", finalTotal);
        response.put("voucherValid", voucherValid);
        response.put("voucherMessage", voucherMessage);
        response.put("appliedVoucherCode", voucherValid ? appliedCode : null); // Trả về mã đã áp dụng để UI hiển thị

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ ĐẶT HÀNG & THANH TOÁN
    // =========================================================================

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody CheckoutRequest request, HttpServletRequest httpReq) {
        try {
            // Ghi đè phí ship để bảo mật
            request.setShippingFee(FIXED_SHIPPING_FEE);

            String clientIp = getClientIp(httpReq);
            CheckoutResponse response = checkoutService.placeOrder(request, clientIp);
            return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new CheckoutResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new CheckoutResponse(false, "Đã có lỗi hệ thống xảy ra.", null));
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
                    ? "/checkout/success?id=" + orderCode
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