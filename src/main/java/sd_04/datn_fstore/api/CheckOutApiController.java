package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.*;
import sd_04.datn_fstore.service.CheckoutService;
import sd_04.datn_fstore.service.VnPayService;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutApiController {

    private final CheckoutService checkoutService;
    private final VnPayService vnPayService;

    // =========================================================================
    // 1. API TÍNH TOÁN TỔNG TIỀN (GỌI KHI CHỌN VOUCHER / ĐỔI SỐ LƯỢNG)
    // =========================================================================
    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody CalculateTotalRequest request) {
        try {
            CalculateTotalResponse response = checkoutService.calculateOrderTotal(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Trả về lỗi 400 để JS hiển thị thông báo
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi tính toán: " + e.getMessage()));
        }
    }

    // =========================================================================
    // 2. API ĐẶT HÀNG (CORE)
    // =========================================================================
    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody CheckoutRequest request, HttpServletRequest httpReq) {
        try {
            // 1. Lấy IP khách hàng (Bắt buộc cho giao dịch VNPAY để bảo mật)
            String clientIp = getClientIp(httpReq);

            // 2. Gọi Service xử lý logic (Tạo đơn, trừ kho, tạo link VNPAY...)
            CheckoutResponse response = checkoutService.placeOrder(request, clientIp);

            // 3. Trả kết quả về cho JS
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (RuntimeException e) {
            // Lỗi logic (Hết hàng, Voucher lỗi...) -> Trả về 400
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new CheckoutResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            // Lỗi hệ thống (DB, Mạng...) -> Trả về 500
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new CheckoutResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    // =========================================================================
    // 3. API XỬ LÝ HỒI ĐÁP TỪ VNPAY (CALLBACK / RETURN URL)
    // =========================================================================
    @GetMapping("/vnpay-return")
    public void handleVnPayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Lấy toàn bộ tham số từ URL trả về của VNPAY
        Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()[0]
                ));

        // Lấy SecureHash riêng để verify
        String secureHash = request.getParameter("vnp_SecureHash");
        if (secureHash != null) {
            vnpParams.put("vnp_SecureHash", secureHash);
        }

        try {
            // 2. Gọi VnPayService để xử lý (Verify Hash, Update đơn hàng, Trừ kho...)
            // Kết quả: 1 (Thành công), 0 (Thất bại/Hủy), -1 (Lỗi Hash)
            int result = vnPayService.orderReturn(vnpParams);

            // Lấy Mã hóa đơn để hiển thị
            String orderCode = vnpParams.get("vnp_TxnRef");

            // 3. CHUYỂN HƯỚNG TRÌNH DUYỆT (REDIRECT)
            if (result == 1) {
                // THÀNH CÔNG -> Chuyển sang trang Cảm ơn
                response.sendRedirect("/checkout/success?id=" + orderCode);
            } else {
                // THẤT BẠI -> Quay lại trang Checkout kèm mã lỗi
                response.sendRedirect("/checkout?error=payment_failed&code=" + orderCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Lỗi hệ thống -> Chuyển trang lỗi
            response.sendRedirect("/checkout?error=system_error");
        }
    }

    // --- HÀM TIỆN ÍCH LẤY IP ---
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Nếu IP có dạng "ip1, ip2", lấy cái đầu tiên
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}