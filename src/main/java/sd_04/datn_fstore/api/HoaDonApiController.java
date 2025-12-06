package sd_04.datn_fstore.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.service.HoaDonChiTietService;
import sd_04.datn_fstore.service.HoaDonExportService;
import sd_04.datn_fstore.service.HoaDonService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hoadon")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Cho phép gọi API từ mọi nguồn (tránh lỗi CORS khi dev)
public class HoaDonApiController {

    private final HoaDonService hoaDonService;
    private final HoaDonExportService hoaDonExportService;
    private final HoaDonChiTietService hoaDonChiTietService;

    // --- 1. API SEARCH (ĐÃ SỬA LỖI PROXY & DÙNG DTO) ---
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<?> searchFull(
            @PageableDefault(size = 10, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) List<Integer> trangThaiList,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        // Gọi service lấy Page<Entity>
        Page<HoaDon> hoaDonPage = hoaDonService.search(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword, minPrice, maxPrice
        );

        // QUAN TRỌNG: Convert Entity sang DTO để tránh lỗi Hibernate Proxy và Infinite Recursion
        Page<HoaDonResponse> dtoPage = hoaDonPage.map(this::convertToDTO);

        return ResponseEntity.ok(dtoPage);
    }

    // --- 2. CÁC API KHÁC ---

    // Cập nhật trạng thái hóa đơn
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

    // Lấy thông tin chi tiết hóa đơn (Header) theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getHoaDonById(@PathVariable Integer id) {
        return hoaDonService.getById(id)
                .map(hd -> ResponseEntity.ok(convertToDTO(hd))) // Convert sang DTO ngay
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy danh sách sản phẩm trong hóa đơn
    @GetMapping("/{hoaDonId}/chi-tiet")
    public ResponseEntity<?> getHoaDonChiTietList(@PathVariable Integer hoaDonId) {
        List<HoaDonChiTiet> list = hoaDonChiTietService.findByHoaDonId(hoaDonId);
        // Lưu ý: Nếu Entity HoaDonChiTiet có quan hệ ngược về HoaDon, hãy đảm bảo có @JsonIgnore bên Entity
        return ResponseEntity.ok(list);
    }

    // Xuất hóa đơn ra PDF
    @GetMapping("/export/pdf/{hoaDonId}")
    public ResponseEntity<byte[]> exportHoaDonPdf(@PathVariable Integer hoaDonId) {
        try {
            byte[] pdfContent = hoaDonExportService.exportHoaDon(hoaDonId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "hoa_don_" + hoaDonId + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 3. HELPER METHODS & DTO ---

    /**
     * Hàm chuyển đổi từ Entity HoaDon sang DTO HoaDonResponse.
     * Xử lý vấn đề Lazy Loading của Hibernate khi truy cập KhachHang.
     */
    private HoaDonResponse convertToDTO(HoaDon hd) {
        String tenKhach = "Khách lẻ";
        String sdtKhach = "";

        // Kiểm tra null và lấy thông tin khách hàng an toàn
        if (hd.getKhachHang() != null) {
            try {
                // Access vào thuộc tính để trigger load dữ liệu (nếu lazy) hoặc lấy giá trị
                tenKhach = hd.getKhachHang().getTenKhachHang();
                sdtKhach = hd.getKhachHang().getSoDienThoai();
            } catch (Exception e) {
                // Nếu xảy ra lỗi proxy hoặc không load được, set giá trị mặc định
                tenKhach = "Không xác định";
            }
        }

        return new HoaDonResponse(
                hd.getId(),
                hd.getMaHoaDon(),
                new HoaDonResponse.KhachHangDTO(tenKhach, sdtKhach),
                hd.getTrangThai(),
                hd.getHinhThucBanHang(),
                hd.getNgayTao(),
                hd.getTongTien(),
                hd.getTongTien() // Nếu bạn có trường tongTienSauGiam thì thay vào đây
        );
    }

    // Inner Class DTO - Dùng để trả về JSON sạch sẽ
    @Data
    @AllArgsConstructor
    public static class HoaDonResponse {
        private Integer id;
        private String maHoaDon;
        private KhachHangDTO khachHang;
        private Integer trangThai;
        private Integer hinhThucBanHang;
        private LocalDateTime ngayTao;
        private BigDecimal tongTien;
        private BigDecimal tongTienSauGiam;

        @Data
        @AllArgsConstructor
        public static class KhachHangDTO {
            private String tenKhachHang;
            private String sdt;
        }
    }
}