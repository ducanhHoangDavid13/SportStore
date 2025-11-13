package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.HoaDonService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
// Đã giữ prefix /admin/ cho mục đích bảo mật
@RequestMapping("/api/admin/hoadon")
@RequiredArgsConstructor
public class HoaDonApiController {

    private final HoaDonService hoaDonService;

    /**
     * API CHÍNH: Lấy danh sách HĐ (Phân trang và Lọc đầy đủ)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<HoaDon>> searchFull(
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) List<Integer> trangThaiList,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            // SỬA: Đã thêm lại tham số ngayKetThuc để hoàn thành bộ lọc ngày
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        // SỬA: Đã truyền đủ 7 tham số (bao gồm ngayKetThuc) vào service
        Page<HoaDon> hoaDonPage = hoaDonService.search(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword, minPrice, maxPrice
        );
        return ResponseEntity.ok(hoaDonPage);
    }

    /**
     * API 2: Cập nhật trạng thái
     * POST /api/admin/hoadon/update-status
     */
    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(
            @RequestParam("hoaDonId") Integer hoaDonId,
            @RequestParam("newTrangThai") Integer newTrangThai) {

        try {
            hoaDonService.updateTrangThai(hoaDonId, newTrangThai);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    /**
     * API 3: Lấy chi tiết 1 hóa đơn (cho edit/xem chi tiết)
     * GET /api/admin/hoadon/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoaDon> getHoaDonById(@PathVariable Integer id) {
        return hoaDonService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}