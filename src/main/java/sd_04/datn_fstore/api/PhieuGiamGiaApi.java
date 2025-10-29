package sd_04.datn_fstore.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.service.PhieuGiamgiaService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phieu-giam-gia")
public class PhieuGiamGiaApi {
    @Autowired
    private PhieuGiamgiaService phieuGiamGiaService;

    @GetMapping
    public Page<PhieuGiamGia> getPromotions(
            // SỬA: Bỏ required=true (mặc định) để cho phép trangThai là NULL
            @RequestParam(name = "trangThai", required = false) Integer trangThai,
            @RequestParam(name = "keyword", required = false) Optional<String> keyword,
            @RequestParam(name = "ngayBatDau", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ngayBatDau,
            @RequestParam(name = "ngayKetThuc", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ngayKetThuc,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {

        String searchKeyword = keyword.orElse(null);

        // Vẫn truyền trangThai (có thể là NULL) vào service
        return phieuGiamGiaService.searchAndFilter(
                trangThai, // <--- trangThai có thể là NULL
                searchKeyword,
                ngayBatDau,
                ngayKetThuc,
                page,
                size,
                sortField,
                sortDir);
    }

    @PostMapping
    public ResponseEntity<PhieuGiamGia> createPromotion(@RequestBody PhieuGiamGia phieuGiamGia) {
        try {
            PhieuGiamGia savedPgg = phieuGiamGiaService.saveWithStatusCheck(phieuGiamGia);
            return new ResponseEntity<>(savedPgg, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi validation hoặc trùng lặp mã
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    // =========================================================
    // THÊM: API CẬP NHẬT VÀ XÓA
    // =========================================================

    // THÊM: GET BY ID (Cần cho Frontend lấy dữ liệu để hiển thị trên form sửa)
    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> getById(@PathVariable Integer id) {
        Optional<PhieuGiamGia> phieu = phieuGiamGiaService.findById(id);
        return phieu.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // THÊM: PUT (Cập nhật)
    @PutMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> updatePromotion(@PathVariable Integer id, @RequestBody PhieuGiamGia phieuGiamGia) {
        try {
            PhieuGiamGia updatedPhieu = phieuGiamGiaService.update(id, phieuGiamGia);
            return ResponseEntity.ok(updatedPhieu);
        } catch (IllegalArgumentException e) {
            // Bắt lỗi validation (như trùng lặp mã)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            // Bắt lỗi Runtime chung (dùng cho lỗi "Không tìm thấy" ném từ Service)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    // THÊM: DELETE (Xóa mềm - Chuyển trạng thái thành 2)
    @DeleteMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> softDeletePromotion(@PathVariable Integer id) {
        try {
            PhieuGiamGia deletedPhieu = phieuGiamGiaService.softDelete(id);
            return ResponseEntity.ok(deletedPhieu);
        } catch (RuntimeException e) {
            // Lỗi không tìm thấy ID
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/sync-status")
    public ResponseEntity<Map<String, Integer>> syncStatus() {
        int count = phieuGiamGiaService.syncPromotionStatus();

        // Trả về số lượng bản ghi đã được cập nhật
        return ResponseEntity.ok(Collections.singletonMap("updatedCount", count));
    }
}
