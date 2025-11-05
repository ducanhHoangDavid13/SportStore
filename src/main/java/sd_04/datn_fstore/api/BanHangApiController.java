package sd_04.datn_fstore.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;
import java.util.Map;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
public class BanHangApiController {

    private final BanHangService banHangService;

    /**
     * Endpoint cho nút "Hoàn tất Thanh toán"
     */
    @PostMapping("/thanh-toan")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> requestBody) {
        try {
            // requestBody chứa "giỏ hàng" (itemsList) và thông tin (khachHangId)
            HoaDon hoaDon = banHangService.createPosPayment(requestBody);
            return ResponseEntity.ok(hoaDon);
        } catch (RuntimeException e) {
            // Trả về lỗi nếu (ví dụ: hết hàng)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint cho nút "Lưu Tạm" (Lưu giỏ hàng)
     */
    @PostMapping("/luu-tam")
    public ResponseEntity<?> saveDraft(@RequestBody Map<String, Object> requestBody) {
        try {
            HoaDon hoaDon = banHangService.saveDraftOrder(requestBody);
            return ResponseEntity.ok(hoaDon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}