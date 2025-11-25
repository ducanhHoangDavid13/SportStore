package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import sd_04.datn_fstore.service.FileStorageService;
import sd_04.datn_fstore.service.HinhAnhService;
import sd_04.datn_fstore.service.SanPhamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import sd_04.datn_fstore.model.HinhAnh;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class SanPhamApiController {

    private final SanPhamService sanPhamService;
    private final SanPhamRepository sanPhamRepository;
    private final FileStorageService fileStorageService;
    private final HinhAnhService hinhAnhService;

    /**
     * API: Lấy danh sách sản phẩm (phân trang, tìm kiếm, lọc)
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
     * API: Lấy chi tiết 1 sản phẩm
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
     * API: Thêm mới sản phẩm CÓ ẢNH (Multipart/form-data)
     */
    @PostMapping("/create-with-image")
    public ResponseEntity<?> createWithImage(
            @RequestParam("sanPhamData") String sanPhamDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 1. Chuyển đổi JSON string thành đối tượng SanPham Entity
            SanPham sanPham = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            // Kiểm tra trùng mã
            if (sanPham.getMaSanPham() != null && !sanPham.getMaSanPham().trim().isEmpty()
                    && sanPhamService.existsByMaSanPham(sanPham.getMaSanPham())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mã sản phẩm đã tồn tại.");
            }

            // Set ngày tạo
            sanPham.setNgayTao(LocalDateTime.now());

            // Lưu sản phẩm
            SanPham savedSanPham = sanPhamService.save(sanPham);

            // 2. Nếu có file, lưu file và tạo Entity HinhAnh
            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setTrangThai(1); // Ảnh đại diện (Active)
                hinhAnh.setSanPham(savedSanPham);
                hinhAnhService.save(hinhAnh);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedSanPham);

        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm và ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm mới: " + e.getMessage());
        }
    }

    /**
     * API MỚI: Cập nhật sản phẩm CÓ ẢNH (Multipart/form-data)
     * Endpoint: /api/san-pham/update-with-image/{id}
     */
    @PutMapping(value = "/update-with-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateWithImage(
            @PathVariable Integer id,
            @RequestParam("sanPhamData") String sanPhamDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 1. Tìm sản phẩm cũ
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();

            // 2. Parse dữ liệu mới từ JSON
            SanPham sanPhamDetails = objectMapper.readValue(sanPhamDataJson, SanPham.class);

            // 3. Cập nhật thông tin (Không cập nhật NgayTao)
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setSoLuong(sanPhamDetails.getSoLuong());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);

            // 4. Xử lý ảnh nếu có upload ảnh mới
            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);

                // Tạo ảnh mới
                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setTenHinhAnh(fileName);
                hinhAnh.setTrangThai(1);
                hinhAnh.setSanPham(updatedSanPham);
                hinhAnhService.save(hinhAnh);

                // (Tùy chọn) Nếu muốn set ảnh này làm ảnh chính và ẩn các ảnh cũ:
                // Bạn có thể gọi thêm logic set các ảnh cũ về trangThai = 0 ở đây
            }

            return ResponseEntity.ok(updatedSanPham);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Lỗi dữ liệu (trùng mã...).");
        } catch (Exception e) {
            log.error("Error updating SanPham with Image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    /**
     * API: Cập nhật thông thường (JSON only) - Giữ lại để tương thích cũ nếu cần
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPhamDetails) {
        try {
            Optional<SanPham> optionalSanPham = sanPhamService.getById(id);
            if (optionalSanPham.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SanPham existingSanPham = optionalSanPham.get();

            existingSanPham.setTenSanPham(sanPhamDetails.getTenSanPham());
            existingSanPham.setMaSanPham(sanPhamDetails.getMaSanPham());
            existingSanPham.setGiaTien(sanPhamDetails.getGiaTien());
            existingSanPham.setSoLuong(sanPhamDetails.getSoLuong());
            existingSanPham.setMoTa(sanPhamDetails.getMoTa());
            existingSanPham.setTrangThai(sanPhamDetails.getTrangThai());

            SanPham updatedSanPham = sanPhamService.save(existingSanPham);
            return ResponseEntity.ok(updatedSanPham);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi cập nhật.");
        }
    }

    /**
     * API: Xóa 1 sản phẩm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            if (!sanPhamService.getById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            // Lưu ý: Cần xử lý ràng buộc khóa ngoại (HinhAnh, ChiTietSP...) trước khi xóa
            sanPhamRepository.deleteById(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Không thể xóa sản phẩm này vì đang được sử dụng.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa.");
        }
    }
}