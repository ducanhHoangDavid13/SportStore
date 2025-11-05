package sd_04.datn_fstore.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.CreateOrderRequest; // <-- SỬ DỤNG DTO
import sd_04.datn_fstore.dto.PaymentNotificationDto; // <-- SỬ DỤNG DTO
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép JavaScript (frontend) gọi vào
public class BanHangApiController {

    private final BanHangService banHangService;

    /**
     * Endpoint cho nút "Hoàn tất Thanh toán"
     */
    @PostMapping("/thanh-toan")
    public ResponseEntity<?> createPayment(@RequestBody CreateOrderRequest request) { // <-- SỬ DỤNG DTO
        try {
            // Gọi service với DTO
            HoaDon hoaDon = banHangService.createPosPayment(request);
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
    public ResponseEntity<?> saveDraft(@RequestBody CreateOrderRequest request) { // <-- SỬ DỤNG DTO
        try {
            // Gọi service với DTO
            HoaDon hoaDon = banHangService.saveDraftOrder(request);
            return ResponseEntity.ok(hoaDon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo mã QR động cho đơn hàng
     * (Hàm này có thể giữ nguyên vì nó chỉ tạo link)
     */
    @PostMapping("/generate-qr")
    public ResponseEntity<?> generateQrCode(@RequestBody Map<String, Object> requestBody) {
        try {
            // ----- CẤU HÌNH CỐ ĐỊNH CỦA BẠN -----
            final String BANK_ID = "970415"; // Ví dụ: Vietinbank
            final String ACCOUNT_NO = "123456789"; // STK của cửa hàng
            // ------------------------------------

            String amount = requestBody.get("amount").toString();
            String orderCode = requestBody.get("orderCode").toString();

            // Mã hóa nội dung (quan trọng)
            String encodedOrderCode = URLEncoder.encode(orderCode, StandardCharsets.UTF_8.toString());

            // Tạo link QR theo chuẩn
            String qrLink = String.format(
                    "https://img.vietqr.io/image/%s-%s.png?amount=%s&addInfo=%s",
                    BANK_ID, ACCOUNT_NO, amount, encodedOrderCode
            );

            // Trả về link ảnh QR cho frontend
            return ResponseEntity.ok(Map.of("qrLink", qrLink));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API Webhook: Dành cho dịch vụ (Casso/VietQR) gọi vào khi có tiền về
     */
    @PostMapping("/payment-notify")
    public ResponseEntity<?> handlePaymentNotification(@RequestBody PaymentNotificationDto paymentData) { // <-- SỬ DỤNG DTO
        try {
            System.out.println("Nhận được Webhook thanh toán: " + paymentData.toString());

            // Gọi service với DTO
            banHangService.confirmPaymentByOrderCode(paymentData);

            // Trả về 200 OK để báo cho dịch vụ là đã nhận thành công
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Nếu có lỗi (vd: tiền không khớp, đơn không tồn tại), trả về 400
            System.err.println("Lỗi xử lý Webhook: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}