package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.TheLoai;
import sd_04.datn_fstore.service.TheLoaiService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/the-loai")
@RequiredArgsConstructor
public class TheLoaiApiController {

    private final TheLoaiService theLoaiService;

    /**
     * BỔ SUNG: Lấy danh sách (GET) có phân trang, tìm kiếm, lọc
     */
    @GetMapping
    public ResponseEntity<Page<TheLoai>> getAllPaginated(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai) {

        Page<TheLoai> theLoaiPage = theLoaiService.searchAndPaginate(pageable, keyword, trangThai);
        return ResponseEntity.ok(theLoaiPage);
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable("id") Integer id, @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(theLoaiService.updateTrangThai(id, trangThai));
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
    public ResponseEntity<TheLoai> addTheLoai(@RequestBody TheLoai theLoai) {
        try {
            // Đảm bảo ID là null để server không ghi đè lên đối tượng cũ
            theLoai.setId(null);
            TheLoai savedTheLoai = theLoaiService.save(theLoai);
            return new ResponseEntity<>(savedTheLoai, HttpStatus.CREATED);
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
    public ResponseEntity<TheLoai> updateTheLoai(@PathVariable Integer id,
                                                 @RequestBody TheLoai theLoai) {
        try {
            // Gán ID từ URL vào đối tượng
            // Điều này đảm bảo chúng ta đang cập nhật đúng đối tượng
            theLoai.setId(id);
            TheLoai updatedTheLoai = theLoaiService.save(theLoai);
            return ResponseEntity.ok(updatedTheLoai);
        } catch (Exception e) {
            // Lỗi này có thể là do gửi ID không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 2. READ (Lấy chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<TheLoai> getById(@PathVariable Integer id) {
        Optional<TheLoai> theLoai = theLoaiService.getById(id);
        return theLoai.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. DELETE (Xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTheLoai(@PathVariable Integer id) {
        try {
            theLoaiService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa Thể Loại này do ràng buộc dữ liệu.");
        }
    }
}