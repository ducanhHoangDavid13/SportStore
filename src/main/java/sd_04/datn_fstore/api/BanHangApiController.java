package sd_04.datn_fstore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.VnPayService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
public class BanHangApiController {

    private final BanHangService banHangService;
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
     * SỬA: API này CHỈ dùng cho "Lưu Tạm" (Trạng thái 0)
     */
    @PostMapping("/luu-tam")
    public ResponseEntity<?> luuHoaDonTam(@RequestBody CreateOrderRequest request) {
        try {
            // Đảm bảo PTTT là "Lưu Tạm" để service set trạng thái = 0
            request.setPaymentMethod("DRAFT");
            HoaDon hoaDon = banHangService.luuHoaDonTam(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API Tạo Link VNPAY
     */
    @PostMapping("/tao-thanh-toan-vnpay")
    public ResponseEntity<?> createVnPayPayment(@RequestBody CreateOrderRequest request,
                                                HttpServletRequest httpReq) {
        try {
            // Gọi service VNPAY, service này sẽ tự động:
            // 1. Gọi banHangService.luuHoaDonTam() (với PTTT là VNPAY/QR)
            // 2. Tạo link VNPAY
            String paymentUrl = vnPayService.createOrder(request, httpReq);

            return ResponseEntity.ok(Map.of("success", true, "paymentUrl", paymentUrl));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // (Các API /hoa-don-tam và /hoa-don-tam/{id} giữ nguyên)
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
}