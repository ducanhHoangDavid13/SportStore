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
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.model.PhieuGiamGia;
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
@CrossOrigin(origins = "*")
public class HoaDonApiController {

    private final HoaDonService hoaDonService;
    private final HoaDonExportService hoaDonExportService;
    private final HoaDonChiTietService hoaDonChiTietService;

    // --- 1. API SEARCH (DÙNG DTO) ---
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

        Page<HoaDon> hoaDonPage = hoaDonService.search(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword, minPrice, maxPrice
        );

        // Convert Entity sang DTO
        Page<HoaDonResponse> dtoPage = hoaDonPage.map(this::convertToDTO);

        return ResponseEntity.ok(dtoPage);
    }

    // --- 2. CÁC API KHÁC (GIỮ NGUYÊN) ---

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
                .map(hd -> ResponseEntity.ok(convertToDTO(hd)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{hoaDonId}/chi-tiet")
    public ResponseEntity<?> getHoaDonChiTietList(@PathVariable Integer hoaDonId) {
        List<HoaDonChiTiet> list = hoaDonChiTietService.findByHoaDonId(hoaDonId);
        return ResponseEntity.ok(list);
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 3. HELPER METHODS & DTO ---

    /**
     * Hàm chuyển đổi từ Entity HoaDon sang DTO HoaDonResponse.
     * ĐÃ SỬA: Bổ sung trường Huyện (huyen) trong DiaChiGiaoHangDTO và Hình thức Thanh toán (hinhThucThanhToan).
     */
    private HoaDonResponse convertToDTO(HoaDon hd) {
        // 1. Khách Hàng
        String tenKhach = "Khách lẻ";
        String sdtKhach = "";
        if (hd.getKhachHang() != null) {
            try {
                // Giả định Entity KhachHang có getTenKhachHang và getSoDienThoai
                // Dùng phương thức có sẵn trong KhachHang Entity
                tenKhach = hd.getKhachHang().getTenKhachHang();
                sdtKhach = hd.getKhachHang().getSoDienThoai();
            } catch (Exception e) {
                tenKhach = "Không xác định";
            }
        }
        HoaDonResponse.KhachHangDTO khachHangDTO = new HoaDonResponse.KhachHangDTO(tenKhach, sdtKhach);

        // 2. Voucher
        HoaDonResponse.PhieuGiamGiaDTO phieuGiamGiaDTO = null;
        PhieuGiamGia pgg = hd.getPhieuGiamGia();
        if (pgg != null) {
            phieuGiamGiaDTO = new HoaDonResponse.PhieuGiamGiaDTO(
                    pgg.getMaPhieuGiamGia()
            );
        }

        // 3. Địa Chỉ Giao Hàng
        HoaDonResponse.DiaChiGiaoHangDTO diaChiDTO = null;
        DiaChi dc = hd.getDiaChiGiaoHang();
        if (dc != null) {
            diaChiDTO = new HoaDonResponse.DiaChiGiaoHangDTO(
                    dc.getHoTen(),
                    dc.getSoDienThoai(),
                    dc.getDiaChiCuThe(),
                    dc.getXa(),
                    dc.getHuyen(),
                    dc.getThanhPho()
            );
        }

        return new HoaDonResponse(
                hd.getId(),
                hd.getMaHoaDon(),
                khachHangDTO,
                hd.getTrangThai(),
                hd.getHinhThucBanHang(),
                hd.getHinhThucThanhToan(), // <<<< BỔ SUNG TRƯỜNG NÀY
                hd.getNgayTao(),
                hd.getTongTien(),
                // Đảm bảo lấy tienGiamGia từ Entity
                hd.getTienGiamGia(), // <<<< BỔ SUNG TRƯỜNG NÀY
                hd.getTongTienSauGiam() != null ? hd.getTongTienSauGiam() : hd.getTongTien(),
                hd.getTienKhachDua(),
                hd.getPhiVanChuyen(),
                phieuGiamGiaDTO,
                diaChiDTO
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
        private Integer hinhThucThanhToan; // <<<< BỔ SUNG TRƯỜNG NÀY
        private LocalDateTime ngayTao;
        private BigDecimal tongTien;
        private BigDecimal tienGiamGia; // <<<< BỔ SUNG TRƯỜNG NÀY
        private BigDecimal tongTienSauGiam;
        private BigDecimal tienKhachDua;
        private BigDecimal phiVanChuyen;
        private PhieuGiamGiaDTO phieuGiamGia;
        private DiaChiGiaoHangDTO diaChiGiaoHang;

        @Data
        @AllArgsConstructor
        public static class KhachHangDTO {
            private String tenKhachHang;
            private String sdt;
        }

        @Data
        @AllArgsConstructor
        public static class PhieuGiamGiaDTO {
            private String maPhieuGiamGia;
        }

        // ĐÃ SỬA: Thêm trường 'huyen'
        @Data
        @AllArgsConstructor
        public static class DiaChiGiaoHangDTO {
            private String hoTen;
            private String soDienThoai;
            private String diaChiCuThe;
            private String xa;
            private String huyen; // <-- ĐÃ THÊM
            private String thanhPho;
        }
    }
}