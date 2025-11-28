package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.repository.HoaDonRepository;
import sd_04.datn_fstore.service.HoaDonService;
import sd_04.datn_fstore.service.HoaDonExportService; // Thêm Export Service
import sd_04.datn_fstore.service.HoaDonChiTietService; // Thêm ChiTiet Service

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// SỬA: Thay đổi tên lớp để tránh trùng lặp với file API chung nếu có
@RestController
@RequestMapping("/api/admin/hoadon")
@RequiredArgsConstructor
public class HoaDonApiController {

    private final HoaDonService hoaDonService;
    private final HoaDonExportService hoaDonExportService; // Inject Export Service
    private final HoaDonChiTietService hoaDonChiTietService; // Inject ChiTiet Service

    /**
     * API 1: Lấy danh sách HĐ (Phân trang và Lọc đầy đủ) - Đã giữ nguyên logic
     * GET /api/admin/hoadon/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<HoaDon>> searchFull(
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) List<Integer> trangThaiList,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        Page<HoaDon> hoaDonPage = hoaDonService.search(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword, minPrice, maxPrice
        );
        return ResponseEntity.ok(hoaDonPage);
    }

    /**
     * API 2: Cập nhật trạng thái - Đã giữ nguyên logic
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
     * API 3: Lấy chi tiết 1 hóa đơn (cho edit/xem chi tiết) - Đã giữ nguyên logic
     * GET /api/admin/hoadon/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoaDon> getHoaDonById(@PathVariable Integer id) {
        // Cần đảm bảo hàm getById() trong HoaDonService đã được cấu hình JOIN FETCH HDCT
        return hoaDonService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- BỔ SUNG CÁC API CHO LUỒNG XEM VÀ IN HÓA ĐƠN ---

    /**
     * BỔ SUNG API 4: Lấy danh sách Chi tiết Hóa đơn theo ID Hóa đơn
     * Dùng khi Frontend cần tải riêng HDCT (ví dụ: trong modal hoặc bảng)
     * GET /api/admin/hoadon/10/chi-tiet
     */
    @GetMapping("/{hoaDonId}/chi-tiet")
    public List<HoaDonChiTiet> getHoaDonChiTietList(@PathVariable Integer hoaDonId) {
        return hoaDonChiTietService.findByHoaDonId(hoaDonId);
    }

    /**
     * BỔ SUNG API 5: Xuất PDF để in Hóa đơn
     * GET /api/admin/hoadon/export/pdf/10
     */
    @GetMapping("/export/pdf/{hoaDonId}")
    public ResponseEntity<byte[]> exportHoaDonPdf(@PathVariable Integer hoaDonId) {

        try {
            byte[] pdfContent = hoaDonExportService.exportHoaDon(hoaDonId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "hoa_don_" + hoaDonId + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Log lỗi và trả về lỗi 500
            System.err.println("Lỗi khi tạo file PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}