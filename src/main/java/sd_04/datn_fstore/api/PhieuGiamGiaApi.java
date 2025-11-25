package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.service.PhieuGiamgiaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhieuGiamGiaApi {

    private final PhieuGiamgiaService phieuGiamGiaService;

    // 1. TÌM KIẾM & PHÂN TRANG
    @GetMapping
    public ResponseEntity<Page<PhieuGiamGia>> getPromotions(
            @RequestParam(name = "trangThai", required = false) Integer trangThai,
            @RequestParam(name = "keyword", required = false) Optional<String> keyword,
            @RequestParam(name = "ngayBatDau", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(name = "ngayKetThuc", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        String searchKeyword = keyword.orElse(null);
        Page<PhieuGiamGia> result = phieuGiamGiaService.searchAndFilter(
                trangThai, searchKeyword, ngayBatDau, ngayKetThuc, page, size, sortField, sortDir);
        return ResponseEntity.ok(result);
    }

    // 2. THÊM MỚI
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody PhieuGiamGia phieuGiamGia) {
        try {
            PhieuGiamGia savedPgg = phieuGiamGiaService.saveWithStatusCheck(phieuGiamGia);
            return new ResponseEntity<>(savedPgg, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // 3. LẤY CHI TIẾT
    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> getById(@PathVariable Integer id) {
        return phieuGiamGiaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. CẬP NHẬT
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Integer id, @RequestBody PhieuGiamGia phieuGiamGia) {
        try {
            PhieuGiamGia updatedPhieu = phieuGiamGiaService.update(id, phieuGiamGia);
            return ResponseEntity.ok(updatedPhieu);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // 5. ĐẢO TRẠNG THÁI (Active <-> Inactive) - MỚI
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            phieuGiamGiaService.toggleStatus(id); // Cần đảm bảo hàm này có trong Service
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 6. ĐỒNG BỘ TRẠNG THÁI (Quét tự động)
    @PostMapping("/sync-status")
    public ResponseEntity<?> syncStatus() {
        try {
            phieuGiamGiaService.capNhatTrangThaiTuDong();
            return ResponseEntity.ok(Map.of("message", "Đã đồng bộ trạng thái"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    // 7. DANH SÁCH ACTIVE (Cho trang bán hàng)
    @GetMapping("/active")
    public ResponseEntity<List<PhieuGiamGia>> getActiveVouchers() {
        return ResponseEntity.ok(phieuGiamGiaService.getActive());
    }
}