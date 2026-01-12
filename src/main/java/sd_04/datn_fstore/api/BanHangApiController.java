package sd_04.datn_fstore.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 1. Import log
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.CreateOrderRequest;
import sd_04.datn_fstore.dto.VnPayResponseDTO;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.BanHangService;
import sd_04.datn_fstore.service.HoaDonService;
import sd_04.datn_fstore.service.VnPayService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/ban-hang")
@RequiredArgsConstructor
@Slf4j // 2. Tự động tạo biến log để ghi lỗi
@CrossOrigin("*") // 3. Cho phép Frontend gọi API thoải mái
public class BanHangApiController {

    private final BanHangService banHangService;
    private final HoaDonService hoaDonService;

    @Lazy
    private final VnPayService vnPayService;

    /**
     * API 1: Thanh toán Tiền Mặt (COD) / Tại quầy
     */
    @PostMapping("/thanh-toan")
    public ResponseEntity<?> thanhToanTienMat(@RequestBody CreateOrderRequest request) {
        log.info("Yêu cầu thanh toán tiền mặt cho HĐ ID: {}", request.getMaHoaDon());
        try {
            HoaDon hoaDon = banHangService.thanhToanTienMat(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            log.error("Lỗi thanh toán tiền mặt: ", e); // Ghi lỗi chi tiết ra Console
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Thanh toán thất bại: " + e.getMessage(),
                    "error", e.toString()
            ));
        }
    }

    /**
     * API 2: Lưu Tạm (DRAFT) - Treo đơn hàng
     */
    @PostMapping("/luu-tam")
    public ResponseEntity<?> luuHoaDonTam(@RequestBody CreateOrderRequest request) {
        try {
            request.setPhuongThucThanhToan("DRAFT");
            HoaDon hoaDon = banHangService.luuHoaDonTam(request);
            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            log.error("Lỗi lưu hóa đơn tạm: ", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @DeleteMapping("/hoa-don-tam/{maHoaDon}")
    public ResponseEntity<?> deleteHoaDonTam(@PathVariable String maHoaDon) {
        try {
            // Gọi service để xử lý logic xóa (bao gồm xóa cả sản phẩm bên trong)
            hoaDonService.deleteByMaHoaDon(maHoaDon);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Trả về lỗi 400 kèm thông báo chi tiết
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 5: Lấy danh sách hóa đơn chờ (Sửa lỗi không tải được danh sách)
     */
    @GetMapping("/hoa-don-tam")
    public ResponseEntity<?> getHoaDonTam() {
        try {
            // --- SỬA Ở ĐÂY ---
            // Code cũ: List<HoaDon> drafts = banHangService.getDraftOrders();
            // Code mới: Gọi hàm chuyên biệt cho POS (đã lọc bỏ đơn Online)
            List<HoaDon> drafts = hoaDonService.getHoaDonChoTaiQuay();
            // -----------------

            if (drafts == null || drafts.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(drafts);
        } catch (Exception e) {
            // log.error... giữ nguyên
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/hoa-don-tam/{maHoaDon}") // Nên để là maHoaDon cho rõ nghĩa
    public ResponseEntity<?> getChiTietHoaDonTam(@PathVariable("maHoaDon") String maHoaDon) {
        try {
            // Hàm này lấy chi tiết theo Mã Hóa Đơn (String)
            // Bạn có thể giữ nguyên banHangService nếu nó chỉ đơn thuần là findByMaHoaDon
            HoaDon hoaDon = banHangService.getDraftOrderByCode(maHoaDon);

            // Hoặc an toàn hơn, dùng hoaDonRepository.findByMaHoaDon(maHoaDon)

            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy hóa đơn: " + maHoaDon));
            }

            // [TÙY CHỌN] Nếu muốn chặn tuyệt đối không cho xem đơn Online ở màn hình POS
            if (hoaDon.getHinhThucBanHang() != 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Đây là đơn Online, không thể xử lý tại quầy!"));
            }

            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Utils ---
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress != null ? ipAddress.split(",")[0].trim() : "";
    }
}