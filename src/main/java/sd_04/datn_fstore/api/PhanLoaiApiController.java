package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.PhanLoai;
import sd_04.datn_fstore.service.PhanLoaiService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phan-loai")
@RequiredArgsConstructor
public class PhanLoaiApiController {

    private final PhanLoaiService phanLoaiService;

    /**
     * BỔ SUNG: Lấy danh sách (GET) có phân trang, tìm kiếm, lọc
     */
    @GetMapping
    public ResponseEntity<Page<PhanLoai>> getAllPaginated(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai) {

        Page<PhanLoai> phanLoaiPage = phanLoaiService.searchAndPaginate(pageable, keyword, trangThai);
        return ResponseEntity.ok(phanLoaiPage);
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable("id") Integer id, @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(phanLoaiService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    /**
     * Chỉ xử lý Thêm mới (Create)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PhanLoai> addPhanLoai(@RequestBody PhanLoai phanLoai) {
        try {
            // Đảm bảo ID là null để server không ghi đè lên đối tượng cũ
            phanLoai.setId(null);
            PhanLoai savedPhanLoai = phanLoaiService.save(phanLoai);
            return new ResponseEntity<>(savedPhanLoai, HttpStatus.CREATED);
        } catch (Exception e) {
            // Lỗi này có thể là do validation hoặc lỗi CSDL
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    /**
     * Chỉ xử lý Cập nhật (Update)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PhanLoai> updatePhanLoai(@PathVariable Integer id,
                                                   @RequestBody PhanLoai phanLoai) {
        try {
            // Gán ID từ URL vào đối tượng
            // Điều này đảm bảo chúng ta đang cập nhật đúng đối tượng
            phanLoai.setId(id);
            PhanLoai updatedPhanLoai = phanLoaiService.save(phanLoai);
            return ResponseEntity.ok(updatedPhanLoai);
        } catch (Exception e) {
            // Lỗi này có thể là do gửi ID không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 2. READ (Lấy chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<PhanLoai> getById(@PathVariable Integer id) {
        Optional<PhanLoai> phanLoai = phanLoaiService.getById(id);
        return phanLoai.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. DELETE (Xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePhanLoai(@PathVariable Integer id) {
        try {
            phanLoaiService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa Phân Loại này do ràng buộc dữ liệu.");
        }
    }
}