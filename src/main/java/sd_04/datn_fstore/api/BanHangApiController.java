package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy; // <-- THÊM IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO; // <-- THÊM IMPORT
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.VnPayService; // <-- THÊM IMPORT

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
public class BanHangApiController {

    private final BanHangService banHangService;

    // Tiêm VnPayService (với @Lazy) CHỈ ĐỂ DÙNG CHO CALLBACK
    @Lazy
    private final VnPayService vnPayService;

    /**
     * API Tiền Mặt (Giữ nguyên)
     */
    @PostMapping("/thanh-toan")
    public ResponseEntity<?> thanhToanTienMat(@RequestBody CreateOrderRequest request) {
        try {
            HoaDon hoaDon = banHangService.thanhToanTienMat(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API "Lưu Tạm" (Trạng thái 0) (Giữ nguyên)
     */
    @PostMapping("/luu-tam")
    public ResponseEntity<?> luuHoaDonTam(@RequestBody CreateOrderRequest request) {
        try {
            request.setPaymentMethod("DRAFT");
            HoaDon hoaDon = banHangService.luuHoaDonTam(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * SỬA LẠI: API Tạo Link VNPAY
     */
    @PostMapping("/tao-thanh-toan-vnpay")
    public ResponseEntity<?> createVnPayPayment(@RequestBody CreateOrderRequest request,
                                                HttpServletRequest httpReq) {
        try {
            // 1. Lấy IP
            String clientIp = getClientIp(httpReq);

            // 2. GỌI BANHANGSERVICE (Không gọi VnPayService trực tiếp)
            VnPayResponseDTO response = banHangService.taoThanhToanVnPay(request, clientIp);

            // 3. Trả DTO về JS
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            VnPayResponseDTO errorResponse = new VnPayResponseDTO(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * THÊM MỚI: API NHẬN KẾT QUẢ TỪ VNPAY (IPN / Callback)
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<Map<String, String>> vnpayCallback(@RequestParam Map<String, String> vnpParams) {
        try {
            // Gọi service VNPAY (đã tiêm @Lazy) để xử lý
            int result = vnPayService.orderReturn(vnpParams);

            if (result == 1) { // Thành công
                return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
            } else if (result == 0) { // Thất bại
                return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Order failed or Canceled"));
            } else { // Lỗi
                return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Error or Invalid Signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Exception: " + e.getMessage()));
        }
    }

    // (Các API /hoa-don-tam giữ nguyên)
    @GetMapping("/hoa-don-tam")
    public ResponseEntity<List<HoaDon>> getHoaDonTam() {
        List<HoaDon> drafts = banHangService.getDraftOrders();
        return ResponseEntity.ok(drafts);
    }

    @GetMapping("/hoa-don-tam/{id}")
    public ResponseEntity<HoaDon> getChiTietHoaDonTam(@PathVariable Integer id) {
        HoaDon hoaDon = banHangService.getDraftOrderDetail(id);
        return ResponseEntity.ok(hoaDon);
    }

    // THÊM: Hàm tiện ích lấy IP
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress.split(",")[0].trim();
    }
}