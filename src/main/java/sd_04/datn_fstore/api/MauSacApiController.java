package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.MauSac;
import sd_04.datn_fstore.service.MauSacService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
public class MauSacApiController {

    private final MauSacService mauSacService;

    /**
     * BỔ SUNG: Lấy danh sách (GET) có phân trang, tìm kiếm, lọc
     */
    @GetMapping
    public ResponseEntity<Page<MauSac>> getAllPaginated(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai) {

        Page<MauSac> mauSacPage = mauSacService.searchAndPaginate(pageable, keyword, trangThai);
        return ResponseEntity.ok(mauSacPage);
    }

    // 1. CREATE & UPDATE (Save)
    /**
     * Chỉ xử lý Thêm mới (Create)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MauSac> addMauSac(@RequestBody MauSac mauSac) {
        try {
            // Đảm bảo ID là null để server không ghi đè lên đối tượng cũ
            mauSac.setId(null);
            MauSac savedMauSac = mauSacService.save(mauSac);
            return new ResponseEntity<>(savedMauSac, HttpStatus.CREATED);
        } catch (Exception e) {
            // Lỗi này có thể là do validation hoặc lỗi CSDL
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(mauSacService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
    /**
     * Chỉ xử lý Cập nhật (Update)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MauSac> updateMauSac(@PathVariable Integer id,
                                               @RequestBody MauSac mauSac) {
        try {
            // Gán ID từ URL vào đối tượng
            // Điều này đảm bảo chúng ta đang cập nhật đúng đối tượng
            mauSac.setId(id);
            MauSac updatedMauSac = mauSacService.save(mauSac);
            return ResponseEntity.ok(updatedMauSac);
        } catch (Exception e) {
            // Lỗi này có thể là do gửi ID không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 2. READ (Lấy chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<MauSac> getById(@PathVariable Integer id) {
        Optional<MauSac> mauSac = mauSacService.getById(id);
        return mauSac.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. DELETE (Xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMauSac(@PathVariable Integer id) {
        try {
            mauSacService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa Màu Sắc này do ràng buộc dữ liệu.");
        }
    }
}