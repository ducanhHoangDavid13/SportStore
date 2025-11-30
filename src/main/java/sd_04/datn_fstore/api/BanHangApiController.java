package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.VnPayService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
public class BanHangApiController {

    private final BanHangService banHangService;

    // Tiêm VnPayService (với @Lazy để tránh vòng lặp dependencies)
    @Lazy
    private final VnPayService vnPayService;

    /**
     * API 1: Thanh toán Tiền Mặt (COD) / Tại quầy
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
     * API 2: Lưu Tạm (DRAFT)
     */
    @PostMapping("/luu-tam")
    public ResponseEntity<?> luuHoaDonTam(@RequestBody CreateOrderRequest request) {
        try {
            // CẬP NHẬT: Dùng tên biến Tiếng Việt khớp với DTO mới
            request.setPhuongThucThanhToan("DRAFT");

            HoaDon hoaDon = banHangService.luuHoaDonTam(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API 3: Tạo Link VNPAY
     */
    @PostMapping("/tao-thanh-toan-vnpay")
    public ResponseEntity<?> createVnPayPayment(@RequestBody CreateOrderRequest request,
                                                HttpServletRequest httpReq) {
        try {
            String clientIp = getClientIp(httpReq);
            VnPayResponseDTO response = banHangService.taoThanhToanVnPay(request, clientIp);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            VnPayResponseDTO errorResponse = new VnPayResponseDTO(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API 4: Callback/IPN từ VNPAY sau khi thanh toán xong
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> handleVnPayCallback(HttpServletRequest request) {
        // Lấy toàn bộ tham số trả về từ VNPAY
        Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()[0]
                ));

        String secureHash = request.getParameter("vnp_SecureHash");
        if (secureHash != null) {
            vnpParams.put("vnp_SecureHash", secureHash);
        }

        try {
            // Gọi Service xử lý (Kiểm tra hash, cập nhật trạng thái đơn, trừ kho)
            int result = vnPayService.orderReturn(vnpParams);

            String message;
            if (result == 1) {
                message = "Giao dịch thành công.";
            } else if (result == 0) {
                message = "Giao dịch thất bại (Không đủ tiền, hủy ngang...).";
            } else {
                message = "Lỗi xác thực hoặc lỗi hệ thống.";
            }

            return ResponseEntity.ok(Map.of("code", result, "message", message));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi xử lý callback: " + e.getMessage()));
        }
    }

    // API lấy danh sách hóa đơn tạm (Cho bán hàng tại quầy)
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

    // Hàm tiện ích lấy IP Client
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress.split(",")[0].trim();
    }
}