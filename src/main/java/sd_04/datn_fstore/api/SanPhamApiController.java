package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.SanPhamService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class SanPhamApiController {

    private final SanPhamService sanPhamService;
    private  final SanPhamRepository sanPhamRepository;

    /**
     * API: Lấy danh sách sản phẩm (phân trang, tìm kiếm, lọc)
     * ... (Giữ nguyên)
     */
    @GetMapping
    public ResponseEntity<?> search(
            Pageable pageable,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "trangThai", required = false) Integer trangThai) {
        try {
            Page<SanPham> sanPhamPage = sanPhamService.searchAndPaginate(pageable, keyword, trangThai);
            return ResponseEntity.ok(sanPhamPage);
        } catch (Exception e) {
            log.error("Error searching SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm sản phẩm.");
        }
    }

    /**
     * API: Lấy TẤT CẢ sản phẩm (dùng cho dropdown)
     * ... (Giữ nguyên)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<SanPham> sanPhams = sanPhamRepository.findAll();
            return ResponseEntity.ok(sanPhams);
        } catch (Exception e) {
            log.error("Error getting all SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách sản phẩm.");
        }
    }



    /**
     * API: Lấy chi tiết 1 sản phẩm
     * ... (Giữ nguyên)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            Optional<SanPham> sanPhamOptional = sanPhamService.getById(id);
            if (sanPhamOptional.isPresent()) {
                return ResponseEntity.ok(sanPhamOptional.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting SanPham by ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy chi tiết sản phẩm.");
        }
    }

    /**
     * API: Thêm mới 1 sản phẩm
     * (Không cần thay đổi logic, vì Jackson/JPA sẽ tự động ánh xạ Khóa ngoại)
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SanPham sanPham) {
        try {
            // Validate đầu vào cơ bản
            if (sanPham.getTenSanPham() == null || sanPham.getTenSanPham().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống.");
            }
            if (sanPham.getGiaTien() == null || sanPham.getSoLuong() == null) {
                return ResponseEntity.badRequest().body("Giá tiền và số lượng không được để trống.");
            }

            // Kiểm tra trùng mã (chỉ khi mã được nhập)
            if (sanPham.getMaSanPham() != null && !sanPham.getMaSanPham().trim().isEmpty()
                    && sanPhamService.existsByMaSanPham(sanPham.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại.");
            }

            sanPham.setNgayTao(new Date());
            SanPham savedSanPham = sanPhamService.save(sanPham);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while creating SanPham: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu, có thể mã sản phẩm đã tồn tại.");
        } catch (Exception e) {
            log.error("Error creating SanPham: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới sản phẩm.");
        }
    }

    /**
     * API: Cập nhật 1 sản phẩm
     * (Cần cập nhật để set trường hinhAnhChinh)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPhamDetails) {
        try {
            // Validate đầu vào
            if (sanPhamDetails.getTenSanPham() == null || sanPhamDetails.getTenSanPham().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống.");
            }
            if (sanPhamDetails.getGiaTien() == null || sanPhamDetails.getSoLuong() == null) {
                return ResponseEntity.badRequest().body("Giá tiền và số lượng không được để trống.");
            }

            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SanPham existingSanPham = optionalSanPham.get();

            // Kiểm tra trùng mã nếu mã bị thay đổi
            if (!existingSanPham.getMaSanPham().equals(sanPhamDetails.getMaSanPham()) &&
                    sanPhamDetails.getMaSanPham() != null && !sanPhamDetails.getMaSanPham().trim().isEmpty() &&
                    sanPhamService.existsByMaSanPham(sanPhamDetails.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm mới này đã thuộc về một sản phẩm khác.");
            }

            // Cập nhật các trường
            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setSoLuong(sanPhamDetails.getSoLuong());

            // ============== CẬP NHẬT TRƯỜNG KHÓA NGOẠI HÌNH ẢNH ================
            // Spring JPA sẽ tự động lấy HinhAnh theo ID được gửi trong sanPhamDetails.getHinhAnhChinh()
//            existingSanPham.setHinhAnhChinh(sanPhamDetails.getHinhAnhChinh());
            // ===================================================================

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);
            return ResponseEntity.ok(updatedSanPham);

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation while updating SanPham ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi ràng buộc dữ liệu khi cập nhật.");
        } catch (Exception e) {
            log.error("Error updating SanPham ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật sản phẩm.");
        }
    }

    /**
     * API: Xóa 1 sản phẩm
     * ... (Giữ nguyên)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (sanPhamService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            sanPhamService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete SanPham ID {} due to foreign key constraint: ", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting SanPham ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}