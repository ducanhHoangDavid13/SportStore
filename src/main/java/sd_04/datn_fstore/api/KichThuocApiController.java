package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.KichThuoc;
import sd_04.datn_fstore.service.KichThuocService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kich-thuoc")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class KichThuocApiController {

    private final KichThuocService kichThuocService;

    /**
     * GET: Lấy danh sách (phân trang, tìm kiếm, lọc)
     */
    @GetMapping
    public ResponseEntity<?> search(
            Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "trangThai", required = false) Integer trangThai) {
        try {
            Page<KichThuoc> kichThuocPage = kichThuocService.searchAndPaginate(pageable, keyword, trangThai);
            return ResponseEntity.ok(kichThuocPage);
        } catch (Exception e) {
            log.error("Error searching KichThuoc: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm kích thước.");
        }
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable("id") Integer id, @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(kichThuocService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
    /**
     * GET: Lấy tất cả (dùng cho dropdown)
     */
    @GetMapping("/all")
    public ResponseEntity<List<KichThuoc>> getAll() {
        return ResponseEntity.ok(kichThuocService.getAll());
    }

    /**
     * GET: Lấy chi tiết 1 kích thước
     */
    @GetMapping("/{id}")
    public ResponseEntity<KichThuoc> getById(@PathVariable Integer id) {
        Optional<KichThuoc> ktOptional = kichThuocService.getById(id);
        return ktOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST: Thêm mới 1 kích thước
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody KichThuoc kichThuoc) {
        try {
            // 1. Validation cơ bản
            if (kichThuoc.getTenKichThuoc() == null || kichThuoc.getTenKichThuoc().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên kích thước không được để trống.");
            }

            // 2. Không cần kiểm tra trùng mã

            KichThuoc savedKichThuoc = kichThuocService.save(kichThuoc);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedKichThuoc);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while creating KichThuoc: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu.");
        } catch (Exception e) {
            log.error("Error creating KichThuoc: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm mới kích thước.");
        }
    }

    /**
     * PUT: Cập nhật 1 kích thước
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody KichThuoc kichThuocDetails) {
        try {
            Optional<KichThuoc> optionalKichThuoc = kichThuocService.getById(id);
            if (optionalKichThuoc.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            KichThuoc existingKichThuoc = optionalKichThuoc.get();

            // 1. Validation cơ bản
            if (kichThuocDetails.getTenKichThuoc() == null || kichThuocDetails.getTenKichThuoc().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên kích thước không được để trống.");
            }

            // 2. Cập nhật các trường
            existingKichThuoc.setTenKichThuoc(kichThuocDetails.getTenKichThuoc());
            existingKichThuoc.setTrangThai(kichThuocDetails.getTrangThai());
            existingKichThuoc.setMoTa(kichThuocDetails.getMoTa()); // Cập nhật trường moTa

            KichThuoc updatedKichThuoc = kichThuocService.save(existingKichThuoc);
            return ResponseEntity.ok(updatedKichThuoc);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while updating KichThuoc ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu khi cập nhật.");
        } catch (Exception e) {
            log.error("Error updating KichThuoc ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật kích thước.");
        }
    }

    /**
     * DELETE: Xóa 1 kích thước
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (kichThuocService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            kichThuocService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete KichThuoc ID {} due to foreign key constraint: ", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting KichThuoc ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}