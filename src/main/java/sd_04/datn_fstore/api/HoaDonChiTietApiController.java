package sd_04.datn_fstore.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.service.HoaDonChiTietService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/hoa-don-chi-tiet")
public class HoaDonChiTietApiController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    /**
     * API 1: Lấy tất cả chi tiết hóa đơn theo ID của hóa đơn cha
     * GET: /api/hoa-don-chi-tiet/by-hoa-don/{hoaDonId}
     */
    @GetMapping("/by-hoa-don/{hoaDonId}")
    public List<HoaDonChiTiet> getByHoaDonId(@PathVariable Integer hoaDonId) {
        return hoaDonChiTietService.findByHoaDonId(hoaDonId);
    }

    /**
     * API 2: Lấy chi tiết hóa đơn bằng ID HDCT
     * GET: /api/hoa-don-chi-tiet/10
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonChiTiet> getById(@PathVariable Integer id) {
        Optional<HoaDonChiTiet> hdct = hoaDonChiTietService.getById(id);
        return hdct.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * API 3: Thêm mới hoặc cập nhật một chi tiết hóa đơn
     * POST/PUT: /api/hoa-don-chi-tiet
     */
    @PostMapping
    public HoaDonChiTiet save(@RequestBody HoaDonChiTiet hoaDonChiTiet) {
        return hoaDonChiTietService.save(hoaDonChiTiet);
    }

    /**
     * API 4: Xóa chi tiết hóa đơn bằng ID
     * DELETE: /api/hoa-don-chi-tiet/10
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return hoaDonChiTietService.getById(id).map(hdct -> {
            hoaDonChiTietService.deleteById(id);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * API 5: XUẤT FILE PDF sử dụng ID Chi tiết Hóa đơn (hoaDonChiTietId)
     * GET: /api/hoa-don-chi-tiet/export/pdf/10
     */
    @GetMapping("/export/pdf/{hoaDonChiTietId}")
    public ResponseEntity<byte[]> exportHoaDonPdfByChiTietId(@PathVariable Integer hoaDonChiTietId) {
        try {
            // Gọi hàm đã implement trong HoaDonChiTietServiceImpl
            byte[] pdfContent = hoaDonChiTietService.exportHoaDonByChiTietId(hoaDonChiTietId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "hoa_don_tu_chitiet_" + hoaDonChiTietId + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Trả về lỗi nếu không tìm thấy HDCT hoặc HD cha
            System.err.println("Lỗi xuất PDF từ HDCT: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}