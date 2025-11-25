package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SanPhamChiTietApiController {

    private final SanPhamCTService sanPhamCTService;

    // API tìm kiếm / lọc
    @GetMapping
    public ResponseEntity<Page<SanPhamChiTiet>> search(
            Pageable pageable,
            @RequestParam(required = false) Integer idSanPham,
            @RequestParam(required = false) Integer idKichThuoc,
            @RequestParam(required = false) Integer idPhanLoai, // Lưu ý: Nếu entity bỏ field này thì param này thừa
            @RequestParam(required = false) Integer idXuatXu,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(required = false) Integer idMauSac,
            @RequestParam(required = false) Integer idTheLoai,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String keyword
    ) {
        // Cần đảm bảo bên Service hàm search cũng nhận đúng tham số này
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, idChatLieu, idTheLoai,
                idXuatXu, idMauSac, idPhanLoai, minPrice, maxPrice, trangThai, keyword
        );
        return ResponseEntity.ok(spctPage);
    }

    // API Thêm mới biến thể
    @PostMapping
    public ResponseEntity<?> addVariant(@RequestBody SanPhamChiTiet sanPhamChiTiet) {
        try {
            sanPhamChiTiet.setId(null);
            SanPhamChiTiet savedSpct = sanPhamCTService.save(sanPhamChiTiet);
            return new ResponseEntity<>(savedSpct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Thêm mới thất bại: " + e.getMessage());
        }
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(sanPhamCTService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // API Cập nhật biến thể
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Integer id,
                                           @RequestBody SanPhamChiTiet dataTuJavaScript) {

        Optional<SanPhamChiTiet> optSpct = sanPhamCTService.getById(id);
        if (optSpct.isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        SanPhamChiTiet spctTrongDB = optSpct.get();

        // 🔴 SỬA LỖI Ở ĐÂY: Dùng setDonGia thay vì setGiaTien
        spctTrongDB.setGiaTien(dataTuJavaScript.getGiaTien());

        spctTrongDB.setSoLuong(dataTuJavaScript.getSoLuong());
        spctTrongDB.setMoTa(dataTuJavaScript.getMoTa());
        spctTrongDB.setTrangThai(dataTuJavaScript.getTrangThai());
        // Cập nhật các mối quan hệ
        spctTrongDB.setSanPham(dataTuJavaScript.getSanPham());
        spctTrongDB.setMauSac(dataTuJavaScript.getMauSac());
        spctTrongDB.setKichThuoc(dataTuJavaScript.getKichThuoc());
        spctTrongDB.setChatLieu(dataTuJavaScript.getChatLieu());
        spctTrongDB.setXuatXu(dataTuJavaScript.getXuatXu());
        spctTrongDB.setTheLoai(dataTuJavaScript.getTheLoai());

        // ⚠️ Lưu ý: Nếu trong Entity SanPhamChiTiet bạn đã bỏ field "phanLoai" thì xóa dòng dưới đi
        // spctTrongDB.setPhanLoai(dataTuJavaScript.getPhanLoai());

        try {
            SanPhamChiTiet updatedSpct = sanPhamCTService.save(spctTrongDB);
            return ResponseEntity.ok(updatedSpct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // API lấy chi tiết
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<SanPhamChiTiet> getById(@PathVariable Integer id) {
        return sanPhamCTService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API Xóa biến thể
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer id) {
        if (sanPhamCTService.getById(id).isEmpty()) {
            return new ResponseEntity<>("Không tìm thấy biến thể với ID: " + id, HttpStatus.NOT_FOUND);
        }
        try {
            sanPhamCTService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa biến thể này vì đang được sử dụng (có trong hóa đơn).");
        }
    }

    @GetMapping("/available")
    public List<SanPhamChiTiet> getAvailableProducts() {
        return sanPhamCTService.getAvailableProducts();
    }

    @GetMapping("/search")
    public List<SanPhamChiTiet> searchBySanPhamTen(@RequestParam("tenSp") String tenSp) {
        return sanPhamCTService.searchBySanPhamTen(tenSp);
    }
}