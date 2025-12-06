package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.XuatXu;
import sd_04.datn_fstore.service.XuatXuService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/xuat-xu")
@RequiredArgsConstructor
public class XuatXuApiController {

    private final XuatXuService xuatXuService;

    /**
     * BỔ SUNG: Lấy danh sách (GET) có phân trang, tìm kiếm, lọc
     */
    @GetMapping
    public ResponseEntity<Page<XuatXu>> getAllPaginated(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai) {

        Page<XuatXu> xuatXuPage = xuatXuService.searchAndPaginate(pageable, keyword, trangThai);
        return ResponseEntity.ok(xuatXuPage);
    }

    // 1. CREATE & UPDATE (Save)
    /**
     * Chỉ xử lý Thêm mới (Create)
     */
    @PostMapping
    public ResponseEntity<XuatXu> addXuatXu(@RequestBody XuatXu xuatXu) {
        try {
            // Đảm bảo ID là null để server không ghi đè lên đối tượng cũ
            xuatXu.setId(null);
            XuatXu savedXuatXu = xuatXuService.save(xuatXu);
            return new ResponseEntity<>(savedXuatXu, HttpStatus.CREATED);
        } catch (Exception e) {
            // Lỗi này có thể là do validation hoặc lỗi CSDL
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable("id") Integer id, @RequestParam("trangThai") Integer trangThai) {
        try {
            return ResponseEntity.ok(xuatXuService.updateTrangThai(id, trangThai));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
    /**
     * Chỉ xử lý Cập nhật (Update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<XuatXu> updateXuatXu(@PathVariable Integer id,
                                               @RequestBody XuatXu xuatXu) {
        try {
            // Gán ID từ URL vào đối tượng
            // Điều này đảm bảo chúng ta đang cập nhật đúng đối tượng
            xuatXu.setId(id);
            XuatXu updatedXuatXu = xuatXuService.save(xuatXu);
            return ResponseEntity.ok(updatedXuatXu);
        } catch (Exception e) {
            // Lỗi này có thể là do gửi ID không tồn tại
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 2. READ (Lấy chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<XuatXu> getById(@PathVariable Integer id) {
        Optional<XuatXu> xuatXu = xuatXuService.getById(id);
        return xuatXu.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. DELETE (Xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteXuatXu(@PathVariable Integer id) {
        try {
            xuatXuService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa Xuất Xứ này do ràng buộc dữ liệu.");
        }
    }
}