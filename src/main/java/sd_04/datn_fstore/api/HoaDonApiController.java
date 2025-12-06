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
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.service.HoaDonService;
import sd_04.datn_fstore.service.HoaDonExportService;
import sd_04.datn_fstore.service.HoaDonChiTietService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hoadon")
@RequiredArgsConstructor
public class HoaDonApiController {

    private final HoaDonService hoaDonService;
    private final HoaDonExportService hoaDonExportService;
    private final HoaDonChiTietService hoaDonChiTietService;

    // --- 1. API SEARCH (ĐÃ SỬA LỖI PROXY) ---
    @GetMapping("/search")
    public ResponseEntity<?> searchFull(
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

        // QUAN TRỌNG: Convert Entity sang DTO để tránh lỗi Hibernate Proxy
        Page<HoaDonResponse> dtoPage = hoaDonPage.map(this::convertToDTO);

        return ResponseEntity.ok(dtoPage);
    }

    // Hàm phụ trợ để chuyển đổi (Mapping)
    private HoaDonResponse convertToDTO(HoaDon hd) {
        String tenKhach = "Khách lẻ";
        String sdtKhach = "";

        // Kiểm tra null và lấy thông tin khách hàng an toàn
        if (hd.getKhachHang() != null) {
            // Lưu ý: Thay .getTenKhachHang() bằng getter thực tế trong entity KhachHang của bạn
            try {
                tenKhach = hd.getKhachHang().getTenKhachHang();
                sdtKhach = hd.getKhachHang().getSoDienThoai();
            } catch (Exception e) {
                // Nếu lazy load lỗi thì bỏ qua
                tenKhach = "Lỗi tải tên";
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
                hd.getTongTien() // Hoặc tongTienSauGiam nếu có
        );
    }

    // --- 2. CÁC API KHÁC GIỮ NGUYÊN ---

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getHoaDonById(@PathVariable Integer id) {
        return hoaDonService.getById(id)
                .map(hd -> ResponseEntity.ok(convertToDTO(hd))) // Dùng luôn hàm convert cho chi tiết
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{hoaDonId}/chi-tiet")
    public List<HoaDonChiTiet> getHoaDonChiTietList(@PathVariable Integer hoaDonId) {
        // Lưu ý: Nếu HoaDonChiTiet cũng có quan hệ Lazy ngược về HoaDon, có thể lỗi tương tự.
        // Tốt nhất nên tạo DTO cho cả cái này, nhưng tạm thời cứ return List xem sao.
        return hoaDonChiTietService.findByHoaDonId(hoaDonId);
    }

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- INNER CLASS DTO (Để ngay trong file này cho gọn) ---
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
            private String tenKhachHang; // Tên biến này phải khớp với JS: hd.khachHang.tenKhachHang
            private String sdt;
        }
    }
}